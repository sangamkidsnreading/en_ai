// ========== Sentence.java (기존 엔티티 업데이트) ==========
package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "english_text", nullable = false, length = 1000)
    private String englishText;  // 영어 문장

    @Column(name = "korean_translation", nullable = false, length = 1000)
    private String koreanTranslation;  // 한국어 번역

    @Column(name = "phonetic", length = 500)
    private String phonetic;  // 발음 기호

    @Column(name = "pronunciation", length = 500)
    private String pronunciation;  // 발음 가이드

    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;  // 난이도 레벨 (1-5)

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;  // 학습 일차

    @Column(name = "audio_url", length = 500)
    private String audioUrl;  // 오디오 파일 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SentenceCategory category;  // 문장 카테고리

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sentence", cascade = CascadeType.ALL)
    private List<UserSentenceProgress> userProgresses;

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