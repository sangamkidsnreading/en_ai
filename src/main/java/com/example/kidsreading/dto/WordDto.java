
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
    
    // 호환성을 위한 필드들
    private String english;
    private String korean;
    private Integer dayNumber;
    private String phonetic;
    private String translation;
    private String word;
}
