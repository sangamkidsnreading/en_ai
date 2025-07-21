package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "badge_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(name = "badge_icon", nullable = false, length = 10)
    private String badgeIcon;

    @Column(name = "badge_description", length = 500)
    private String badgeDescription;

    @Column(name = "attendance_count")
    private Integer attendanceCount; // 출석 횟수

    @Column(name = "streak_count")
    private Integer streakCount; // 연속출석 횟수

    @Column(name = "words_count")
    private Integer wordsCount; // 단어 학습 횟수

    @Column(name = "sentences_count")
    private Integer sentencesCount; // 문장 학습 횟수

    @Column(name = "word_review_count")
    private Integer wordReviewCount; // 복습단어 횟수

    @Column(name = "sentence_review_count")
    private Integer sentenceReviewCount; // 복습문장 횟수

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 