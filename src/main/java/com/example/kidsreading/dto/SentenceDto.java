package com.example.kidsreading.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceDto {
    private Long id;
    private String text;
    private String translation;
    private Integer difficultyLevel;
    private Integer dayNumber;
    private String audioUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Alias methods for compatibility
    public String getEnglish() {
        return text;
    }

    public String getKorean() {
        return translation;
    }

    public Integer getLevel() {
        return difficultyLevel;
    }
}