package com.devteria.profile.repository.httpclient;

import com.devteria.profile.configuration.AuthenticationRequestInterceptor;
import com.devteria.profile.dto.ApiResponse;
import com.devteria.profile.dto.response.FileResponse;
import com.devteria.profile.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.AuthenticationException;
import java.awt.*;

@FeignClient(name = "file-service", url = "http://localhost:8084",
        configuration = {AuthenticationRequestInterceptor.class})
public interface FileClient {
    @PostMapping(value = "/file/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<FileResponse> uploadMedia(@RequestPart("file") MultipartFile file);
}
