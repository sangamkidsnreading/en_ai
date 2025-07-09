
package com.example.kidsreading.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceDto {
    private Long id;
    private String englishText;
    private String koreanTranslation;
    private Integer difficultyLevel;
    private Integer dayNumber;
    private String audioUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 호환성을 위한 필드들
    private String english;
    private String korean;
    private Integer level;
    private String text;
    private String meaning;
    private String translation;
    
    // 빌더 호환성을 위한 메서드
    public Integer getDay() {
        return this.dayNumber;
    }
    
    public void setDay(Integer day) {
        this.dayNumber = day;
    }
}
