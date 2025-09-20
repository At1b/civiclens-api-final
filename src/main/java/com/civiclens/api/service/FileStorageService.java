package com.civiclens.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile multipartFile) {
        File file = null;
        try {
            // 1. Convert MultipartFile to a temporary File
            file = convertMultiPartFileToFile(multipartFile);

            // 2. Generate a unique name for the file in S3
            String uniqueFileName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();

            // 3. Upload the file to the S3 bucket
            s3Client.putObject(new PutObjectRequest(bucketName, uniqueFileName, file));

            // 4. Return the public URL of the uploaded object
            return s3Client.getUrl(bucketName, uniqueFileName).toString();

        } finally {
            // 5. Always delete the temporary local file
            if (file != null) {
                file.delete();
            }
        }
    }

    // --- THIS IS THE NEW, CORRECTED METHOD ---
    private File convertMultiPartFileToFile(MultipartFile multipartFile) {
        try {
            // Create a temporary file in the system's temp directory
            File tempFile = Files.createTempFile("temp", multipartFile.getOriginalFilename()).toFile();

            // Write the multipart file's bytes to the temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(multipartFile.getBytes());
            }
            return tempFile;
        } catch (IOException e) {
            // In a real app, throw a custom exception for better error handling
            throw new RuntimeException("Failed to convert multipart file to file", e);
        }
    }
}
