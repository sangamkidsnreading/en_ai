package com.example.kidsreading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDto {
    private Long id;
    private String text;
    private String meaning;
    private String pronunciation;
    private Integer level;
    private Integer day;
    private String audioUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String english;
    private String korean;
    private Integer dayNumber;
    private String phonetic;
    private String translation;
    private String word;
    private String english;  // Word.text에 매핑
    private String korean;   // Word.meaning에 매핑
}