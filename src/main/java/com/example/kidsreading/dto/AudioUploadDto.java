// ========== AudioUploadDto.java ==========
package com.example.kidsreading.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadDto {
    private String audioUrl;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String uploadStatus;
    private String message;
}