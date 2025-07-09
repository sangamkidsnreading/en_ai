
package com.example.kidsreading.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // 호환성을 위한 별칭 메서드들
    public String getEnglish() {
        return this.text;
    }

    public void setEnglish(String english) {
        this.text = english;
    }

    public String getKorean() {
        return this.meaning;
    }

    public void setKorean(String korean) {
        this.meaning = korean;
    }
}
