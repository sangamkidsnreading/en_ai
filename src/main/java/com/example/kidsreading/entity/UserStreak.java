package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_streaks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "max_streak", nullable = false)
    @Builder.Default
    private Integer maxStreak = 0;

    @Column(name = "last_learning_date")
    private LocalDate lastLearningDate;

    @Column(name = "streak_start_date")
    private LocalDate streakStartDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 연속 학습일 업데이트
     */
    public void updateStreak() {
        LocalDate today = LocalDate.now();
        
        if (lastLearningDate == null) {
            // 첫 학습
            currentStreak = 1;
            streakStartDate = today;
        } else if (lastLearningDate.equals(today)) {
            // 오늘 이미 학습함
            return;
        } else if (lastLearningDate.equals(today.minusDays(1))) {
            // 연속 학습
            currentStreak++;
        } else {
            // 연속 끊김
            currentStreak = 1;
            streakStartDate = today;
        }
        
        lastLearningDate = today;
        
        // 최대 연속일 업데이트
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
    }

    /**
     * 연속 학습일 리셋 (학습을 건너뛴 경우)
     */
    public void resetStreak() {
        currentStreak = 0;
        lastLearningDate = null;
        streakStartDate = null;
    }

    /**
     * 오늘 학습했는지 확인
     */
    public boolean isLearnedToday() {
        return lastLearningDate != null && lastLearningDate.equals(LocalDate.now());
    }

    /**
     * 연속 학습 중인지 확인
     */
    public boolean isOnStreak() {
        return currentStreak > 0;
    }

    /**
     * 연속 학습일 보너스 계산
     */
    public int calculateStreakBonus() {
        if (currentStreak >= 7) {
            return (currentStreak / 7) * 5; // 주당 5코인
        }
        return 0;
    }

    /**
     * 연속 학습일 달성률 (%)
     */
    public double getStreakProgress() {
        if (maxStreak == 0) return 0.0;
        return (double) currentStreak / maxStreak * 100;
    }

    /**
     * 다음 목표까지 남은 일수
     */
    public int getDaysToNextGoal() {
        int[] goals = {1, 3, 7, 14, 30, 60, 100, 365};
        for (int goal : goals) {
            if (currentStreak < goal) {
                return goal - currentStreak;
            }
        }
        return 0; // 모든 목표 달성
    }

    /**
     * 현재 목표
     */
    public int getCurrentGoal() {
        int[] goals = {1, 3, 7, 14, 30, 60, 100, 365};
        for (int goal : goals) {
            if (currentStreak < goal) {
                return goal;
            }
        }
        return 365; // 최대 목표
    }
} 