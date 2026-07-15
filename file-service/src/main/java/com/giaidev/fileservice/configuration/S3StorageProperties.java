package com.giaidev.fileservice.configuration;

import java.time.Duration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;



@Validated
@ConfigurationProperties(prefix = "app.s3")
public record S3StorageProperties(@NotBlank String bucket,
                                  @NotBlank String region,
                                  @NotNull Duration presignedUrlDuration) {

}//@NotBlank bucket and region are not null exists
