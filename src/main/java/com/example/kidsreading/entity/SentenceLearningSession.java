package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentence_learning_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceLearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sentence_id", nullable = false)
    private Long sentenceId;

    @Column(name = "session_start_time", nullable = false)
    private LocalDateTime sessionStartTime;  // 세션 시작 시간

    @Column(name = "session_end_time")
    private LocalDateTime sessionEndTime;  // 세션 종료 시간

    @Column(name = "completion_time_seconds")
    private Integer completionTimeSeconds;  // 완료 시간 (초)

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;  // 완료 여부

    @Column(name = "score", nullable = false)
    private Integer score = 0;  // 획득 점수

    @Column(name = "coins_earned", nullable = false)
    private Integer coinsEarned = 0;  // 획득 코인

    @Column(name = "attempts_count", nullable = false)
    private Integer attemptsCount = 0;  // 시도 횟수

    @Column(name = "mistakes_count", nullable = false)
    private Integer mistakesCount = 0;  // 실수 횟수

    @Column(name = "learning_method", length = 50)
    private String learningMethod;  // 학습 방법 (읽기, 듣기, 말하기 등)

    @Column(name = "notes", length = 1000)
    private String notes;  // 학습 노트

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentence_id", insertable = false, updatable = false)
    private Sentence sentence;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sessionStartTime == null) {
            sessionStartTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void completeSession() {
        this.isCompleted = true;
        this.sessionEndTime = LocalDateTime.now();
        if (this.sessionStartTime != null) {
            this.completionTimeSeconds = (int) java.time.Duration.between(
                this.sessionStartTime, this.sessionEndTime).getSeconds();
        }
    }

    public void addAttempt() {
        this.attemptsCount++;
    }

    public void addMistake() {
        this.mistakesCount++;
    }

    public Double getAccuracy() {
        if (attemptsCount == 0) return 0.0;
        return (double) (attemptsCount - mistakesCount) / attemptsCount * 100;
    }
} 