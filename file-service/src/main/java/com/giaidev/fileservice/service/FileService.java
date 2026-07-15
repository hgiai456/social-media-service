package com.giaidev.fileservice.service;

import com.giaidev.fileservice.dto.response.FileData;
import com.giaidev.fileservice.dto.response.FileResponse;
import com.giaidev.fileservice.exception.AppException;
import com.giaidev.fileservice.exception.ErrorCode;
import com.giaidev.fileservice.mapper.FileMgmtMapper;
import com.giaidev.fileservice.repository.FileMgmtRepository;
import com.giaidev.fileservice.repository.FileRepository;
import com.giaidev.fileservice.storage.FileStorage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {

    FileMgmtRepository fileMgmtRepository;
    FileMgmtMapper fileMgmtMapper;
    FileStorage fileStorage;

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> SUPPORT_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    public FileResponse uploadFile(MultipartFile file) throws IOException {

        validateImage(file);
        //Store file
        var fileInfo = fileStorage.store(file); //upload image

        try {
            //Create file management info
            var fileMgmt = fileMgmtMapper.toFileMgmt(fileInfo);

            // Get userId by SecurityContextHolder
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();

            //set upload userId to FileMgmt
            fileMgmt.setOwnerId(userId);

            //Save fileMgmt to DB
            fileMgmtRepository.save(fileMgmt);


            return FileResponse.builder()
                    .fileId(fileMgmt.getId())
                    .originalFileName(file.getOriginalFilename())
                    .url(fileInfo.getUrl())
                    .build();
        } catch (RuntimeException e) {
            try {
                fileStorage.delete(fileInfo.getPath());
            } catch (RuntimeException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }
    }

    public FileData download(String fileName) throws IOException {
        var fileMgmt = fileMgmtRepository.findById(fileName).orElseThrow(
                () -> new AppException(ErrorCode.FILE_NOT_FOUND));

        var resource = fileStorage.read(fileMgmt.getPath());

        return new FileData(fileMgmt.getContentType(), resource);
    }

    private void validateImage(MultipartFile file){
        if(file == null || file.isEmpty()){
            throw new AppException(ErrorCode.EMPTY_FILE);
        }

        if (file.getSize() > MAX_IMAGE_SIZE){
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        if (!SUPPORT_IMAGE_TYPES.contains(file.getContentType())){
            // if file.getContentType() included in Set SUPPORT_IMAGE_TYPES => false => don't run
            //! Situation if(true) => run
            // if(false) => don't run
            throw new AppException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
    }

    public void deleteFile(String fileName){

        //Get image metadata from MongoDB, if not found => Error
        var fileMgmt = fileMgmtRepository.findById(fileName)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();

        //Only the person who posted the photo has the right to delete it (mới có quyền xóa nó)
        if(!Objects.equals(fileMgmt.getOwnerId(), currentUserId)){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        fileStorage.delete(fileMgmt.getPath()); //Delete in AS3 by path

        fileMgmtRepository.delete(fileMgmt); //Delete in FileMgmt DB
    }


}
