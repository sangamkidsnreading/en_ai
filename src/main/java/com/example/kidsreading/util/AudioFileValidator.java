// ========== AudioFileValidator.java ==========
package com.example.kidsreading.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class AudioFileValidator {

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/aac", "audio/mp4", "audio/x-m4a"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".mp3", ".wav", ".ogg", ".aac", ".m4a"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public static boolean isValidAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 파일 크기 체크
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("파일 크기가 10MB를 초과합니다.");
        }

        // MIME 타입 체크
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("지원하지 않는 오디오 파일 형식입니다.");
        }

        // 파일 확장자 체크
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("파일명이 없습니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("지원하지 않는 파일 확장자입니다.");
        }

        return true;
    }
}