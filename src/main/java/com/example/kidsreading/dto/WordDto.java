package com.example.kidsreading.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
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
    
    // Alias methods for compatibility
    public String getEnglish() {
        return text;
    }
    
    public String getKorean() {
        return meaning;
    }
    private Integer dayNumber;
    private String phonetic;
    private String translation;
    private String word;
    private String english;  // Word.text에 매핑
    private String korean;   // Word.meaning에 매핑
}