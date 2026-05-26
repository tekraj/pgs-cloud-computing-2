package com.todo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3StorageService {

    private final String bucketName;
    private final Region region;

    public S3StorageService(@Value("${app.s3.bucket-name}") String bucketName,
                            @Value("${app.s3.region:us-east-1}") String region) {
        this.bucketName = bucketName;
        this.region = Region.of(region);
    }

    public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        String original = sanitizeFilename(file.getOriginalFilename());
        String key = "profiles/" + userId + "/" + UUID.randomUUID() + "_" + original;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try (S3Client s3Client = S3Client.builder().region(region).build()) {
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        }

        return key;
    }

    public String generateReadUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getRequest)
                .build();

        try (S3Presigner presigner = S3Presigner.builder().region(region).build()) {
            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "upload";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
