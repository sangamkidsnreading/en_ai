package com.example.kidsreading.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class S3Service {

    private final S3Client s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    public S3Service(
        @Value("${cloud.aws.credentials.access-key}") String accessKey,
        @Value("${cloud.aws.credentials.secret-key}") String secretKey,
        @Value("${cloud.aws.region.static}") String region
    ) {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    public String buildS3Key(String type, String fileName) {
        LocalDate today = LocalDate.now();
        String year = String.valueOf(today.getYear());
        String monthDay = String.format("%02d-%02d", today.getMonthValue(), today.getDayOfMonth());

        // Create folder structure: vocabulary/YYYY/MM-DD/words or sentences
        String folderType = "words".equals(type) ? "words" : "sentences";

        // 파일명이 확장자를 포함하지 않는 경우 .mp3 추가
        String finalFileName = fileName;
        if (!fileName.contains(".")) {
            finalFileName = fileName + ".mp3";
        }

        return String.format("vocabulary/%s/%s/%s/%s", year, monthDay, folderType, finalFileName);
    }

    public String uploadFile(MultipartFile file, String type) throws IOException {
        // 원본 파일명 그대로 사용 (정규화하지 않음)
        String originalFileName = file.getOriginalFilename();
        String key = buildS3Key(type, originalFileName);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return key; // S3 경로 반환
    }

    public String uploadFile(File file, String type) throws IOException {
        String originalFileName = file.getName();
        String key = buildS3Key(type, originalFileName);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("audio/wav") // 필요시 확장자별 분기 가능
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        return key;
    }

    public String uploadFileWithOriginalName(File file, String type, String originalFileName) throws IOException {
        String key = buildS3Key(type, originalFileName);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("audio/wav") // 필요시 확장자별 분기 가능
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        return key;
    }

    public boolean fileExists(String s3Key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (Exception e) {
            log.debug("S3 파일이 존재하지 않음: key={}", s3Key);
            return false;
        }
    }

    public String getS3Url(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, 
                s3Client.serviceClientConfiguration().region().id(), s3Key);
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(builder -> builder.bucket(bucket).key(key).build());
    }

    public String uploadFileWithKey(File file, String s3Key, String ext) throws IOException {
        String contentType = "audio/mpeg";
        if (ext.equalsIgnoreCase(".wav")) contentType = "audio/wav";
        else if (ext.equalsIgnoreCase(".m4a")) contentType = "audio/mp4";
        else if (ext.equalsIgnoreCase(".ogg")) contentType = "audio/ogg";
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(contentType)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        // 업로드 성공 여부 확인
        if (!fileExists(s3Key)) {
            throw new IOException("S3 파일 업로드 실패: " + s3Key);
        }
        return s3Key;
    }

    public String uploadFileWithKey(File file, String s3Key) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType("audio/wav") // 필요시 확장자별 분기 가능
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        return s3Key;
    }
}