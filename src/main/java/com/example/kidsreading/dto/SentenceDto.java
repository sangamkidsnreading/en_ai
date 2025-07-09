
package com.example.kidsreading.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentenceDto {
    private Long id;
    private String english;
    private String korean;
    private Integer level;
    private Integer dayNumber;
    private String audioUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 호환성을 위한 별칭 메서드들
    public String getText() {
        return this.english;
    }

    public void setText(String text) {
        this.english = text;
    }

    public String getMeaning() {
        return this.korean;
    }

    public void setMeaning(String meaning) {
        this.korean = meaning;
    }

    public String getTranslation() {
        return this.korean;
    }

    public void setTranslation(String translation) {
        this.korean = translation;
    }

    public Integer getDay() {
        return this.dayNumber;
    }

    public void setDay(Integer day) {
        this.dayNumber = day;
    }
}
