package com.giaidev.fileservice.storage;

import com.giaidev.fileservice.dto.FileInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorage {

    FileInfo store(MultipartFile file) throws IOException; //Input file upload => return metadata FileInfo

    Resource read(String storageKey) throws  IOException; //Read file by storageKey

    void delete(String storageKey); //Delete file by storageKey

    //format storageKey:  images/{uuid}.jpg
}
