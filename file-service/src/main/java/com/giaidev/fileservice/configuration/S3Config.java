package com.giaidev.fileservice.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3Config {

    //S3Client sent request to Amazon S3
    @Bean
    S3Client s3Client(S3StorageProperties properties){
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .build();
    }

    //S3Presigner create URL include signature and expireTime
    @Bean
    S3Presigner s3Presigner(S3StorageProperties properties){
        return S3Presigner.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
