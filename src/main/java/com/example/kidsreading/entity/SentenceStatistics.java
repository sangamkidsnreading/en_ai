package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentence_statistics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sentence_id", nullable = false)
    private Long sentenceId;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts = 0;  // 총 시도 횟수

    @Column(name = "successful_attempts", nullable = false)
    private Integer successfulAttempts = 0;  // 성공한 시도 횟수

    @Column(name = "average_completion_time")
    private Double averageCompletionTime;  // 평균 완료 시간 (초)

    @Column(name = "difficulty_rating")
    private Double difficultyRating;  // 난이도 평점 (1-5)

    @Column(name = "popularity_score")
    private Integer popularityScore = 0;  // 인기도 점수

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;  // 마지막 접근 시간

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentence_id", insertable = false, updatable = false)
    private Sentence sentence;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementAttempts() {
        this.totalAttempts++;
        this.lastAccessedAt = LocalDateTime.now();
    }

    public void incrementSuccessfulAttempts() {
        this.successfulAttempts++;
        this.lastAccessedAt = LocalDateTime.now();
    }

    public Double getSuccessRate() {
        if (totalAttempts == 0) return 0.0;
        return (double) successfulAttempts / totalAttempts * 100;
    }
} 