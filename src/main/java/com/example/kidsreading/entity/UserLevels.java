package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_levels")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "current_level", nullable = false)
    private Integer currentLevel = 1;

    @Column(name = "current_day", nullable = false)
    private Integer currentDay = 1;

    @Column(name = "total_coins", nullable = false)
    private Integer totalCoins = 0;

    @Column(name = "experience_points", nullable = false)
    private Integer experiencePoints = 0;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays = 0;

    @Column(name = "last_learning_date")
    private LocalDate lastLearningDate;

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

    // 비즈니스 메서드들

    /**
     * 코인 추가
     */
    public void addCoins(Integer coins) {
        this.totalCoins += coins;
        this.experiencePoints += coins;
    }

    /**
     * 레벨 업 체크 및 처리
     */
    public boolean checkAndLevelUp(Integer levelUpRequiredCoins) {
        if (this.experiencePoints >= levelUpRequiredCoins) {
            this.currentLevel++;
            this.currentDay = 1;
            this.experiencePoints = 0;
            return true;
        }
        return false;
    }

    /**
     * 다음 날로 진행
     */
    public void nextDay() {
        this.currentDay++;
        this.lastLearningDate = LocalDate.now();
    }

    /**
     * 연속 학습 일수 업데이트
     */
    public void updateStreakDays() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (lastLearningDate == null) {
            // 첫 학습
            this.streakDays = 1;
        } else if (lastLearningDate.equals(yesterday)) {
            // 연속 학습
            this.streakDays++;
        } else if (lastLearningDate.equals(today)) {
            // 오늘 이미 학습함 (변경 없음)
            return;
        } else {
            // 연속 학습 끊김
            this.streakDays = 1;
        }

        this.lastLearningDate = today;
    }

    /**
     * 연속 학습 여부 확인
     */
    public boolean isConsecutiveLearning() {
        if (lastLearningDate == null) return false;

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return lastLearningDate.equals(yesterday) || lastLearningDate.equals(today);
    }
}