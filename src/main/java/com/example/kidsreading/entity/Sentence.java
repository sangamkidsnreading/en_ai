package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sentences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String english;

    @Column(nullable = false, length = 1000)
    private String korean;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 호환성을 위한 getter 메서드들
    public String getEnglish() {
        return this.englishText;
    }

    public String getKorean() {
        return this.koreanTranslation;
    }

    public Integer getLevel() {
        return this.difficultyLevel;
    }

    public String getText() {
        return this.englishText;
    }

    public String getMeaning() {
        return this.koreanTranslation;
    }

    public String getTranslation() {
        return this.koreanTranslation;
    }

    public Integer getDay() {
        return this.dayNumber;
    }

    // 호환성을 위한 setter 메서드들
    public void setEnglish(String english) {
        this.englishText = english;
    }

    public void setKorean(String korean) {
        this.koreanTranslation = korean;
    }

    public void setLevel(Integer level) {
        this.difficultyLevel = level;
    }

    public void setText(String text) {
        this.englishText = text;
    }

    public void setMeaning(String meaning) {
        this.koreanTranslation = meaning;
    }

    public void setTranslation(String translation) {
        this.koreanTranslation = translation;
    }

    public void setDay(Integer day) {
        this.dayNumber = day;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}