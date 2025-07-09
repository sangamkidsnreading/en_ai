package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_word_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    // AdminService에서 필요한 필드들 추가
    @Builder.Default
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Builder.Default
    @Column(name = "incorrect_count", nullable = false)
    private Integer incorrectCount = 0;

    @Builder.Default
    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts = 0;

    // 완료 여부 (AdminService에서 isCompleted로 참조)
    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    // 기존 필드들
    @Builder.Default
    @Column(name = "is_learned", nullable = false)
    private Boolean isLearned = false;

    @Builder.Default
    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Builder.Default
    @Column(name = "learn_count", nullable = false)
    private Integer learnCount = 0;

    @Column(name = "first_learned_at")
    private LocalDateTime firstLearnedAt;

    @Column(name = "last_learned_at")
    private LocalDateTime lastLearnedAt;

    // AdminService에서 lastStudiedAt으로 참조 (last_learned_at과 동일한 값)
    @Column(name = "last_studied_at")
    private LocalDateTime lastStudiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", insertable = false, updatable = false)
    private Word word;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // lastStudiedAt과 lastLearnedAt 동기화
        if (lastLearnedAt != null && lastStudiedAt == null) {
            lastStudiedAt = lastLearnedAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // lastStudiedAt과 lastLearnedAt 동기화
        if (lastLearnedAt != null) {
            lastStudiedAt = lastLearnedAt;
        }

        // 학습 완료 조건 자동 업데이트 (예: 정답률 80% 이상)
        if (totalAttempts > 0) {
            double successRate = (double) correctCount / totalAttempts;
            if (successRate >= 0.8 && totalAttempts >= 3) {
                isCompleted = true;
                isLearned = true;
            }
        }
    }

    // 편의 메서드들
    public void addCorrectAttempt() {
        this.correctCount++;
        this.totalAttempts++;
        this.learnCount++;
        this.lastLearnedAt = LocalDateTime.now();
        this.lastStudiedAt = LocalDateTime.now();

        if (this.firstLearnedAt == null) {
            this.firstLearnedAt = LocalDateTime.now();
        }
    }

    public void addIncorrectAttempt() {
        this.incorrectCount++;
        this.totalAttempts++;
        this.learnCount++;
        this.lastLearnedAt = LocalDateTime.now();
        this.lastStudiedAt = LocalDateTime.now();

        if (this.firstLearnedAt == null) {
            this.firstLearnedAt = LocalDateTime.now();
        }
    }

    public double getSuccessRate() {
        if (totalAttempts == 0) return 0.0;
        return (double) correctCount / totalAttempts;
    }
}