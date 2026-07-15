package com.giaidev.fileservice.storage;

import com.giaidev.fileservice.configuration.S3StorageProperties;
import com.giaidev.fileservice.dto.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3FileStorage implements FileStorage{

    private final S3Client s3Client; //use to upload, read, delete
    private final S3Presigner s3Presigner; //Create Reading Image URL have expireTime
    private final S3StorageProperties properties; //provide bucket, region and URL expireTime
    private static final String IMAGE_PREFIX = "images/";

    private String generateObjectKey(String originalFilename){
        String cleanFilename =
                StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);//Remove URL don't need, just keep original path

        String extension = StringUtils.getFilenameExtension(cleanFilename); //Return jpg or png (type of file)
        String generatedFilename = UUID.randomUUID().toString(); //Generate random name

        if(!StringUtils.hasText(extension) //checking file name is not included special characters (*^&)
                || !extension.matches("[a-zA-Z0-9]+")){
            return IMAGE_PREFIX + generatedFilename;
        }

        return IMAGE_PREFIX
                + generatedFilename
                + "."
                + extension.toLowerCase(Locale.ROOT); //Revert extension to normal text

    } //avatar.JPG → images/550e8400-e29b-41d4-a716-446655440000.jpg

    private String generatePresignedGetUrl(String objectKey){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()//Describe Object need to be read
                .bucket(properties.bucket()) // bookhub-damhoa-2026.
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(properties.presignedUrlDuration()) //URL exist in 10 minutes follow YAML
                .getObjectRequest(getObjectRequest) //Sign request by AWS credentials
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    @Override
    public FileInfo store(MultipartFile file) throws IOException {
        String objectKey = generateObjectKey(file.getOriginalFilename()); //Create the only key

        byte[] content = file.getBytes(); //

        PutObjectRequest putObjectRequest = PutObjectRequest.builder() //Store metadata request sent to S3
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(file.getContentType())
                .contentLength((long) content.length)
                .build();
        log.info("PutObject: {}", putObjectRequest.toString());
        log.info("Request Body : {}",  RequestBody.fromBytes(content));

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(content));  //Upload Image to AWS

        return FileInfo.builder() //Return FileInfo Object
                .name(StringUtils.getFilename(objectKey))
                .contentType(file.getContentType())
                .size(content.length)
                .md5Checksum(DigestUtils.md5DigestAsHex(content))
                .path(objectKey)
                .url(generatePresignedGetUrl(objectKey))
                .build();
    }

    @Override
    public Resource read(String storageKey){
        //storageKey is gotten from FileMgmt.path

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(storageKey)
                .build();  //Define bucket and object need to read

        byte[] content = s3Client
                .getObjectAsBytes(getObjectRequest) //Sent real request to S3 and install object to memory
                .asByteArray();//Revert from response to byte[]

        return new ByteArrayResource(content); //Controller return Image Data by ResponseEntity<Resourse>
    }

    @Override
    public void delete(String storageKey){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(storageKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }





}
