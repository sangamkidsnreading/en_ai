
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
    @Builder.Default
    private Integer currentLevel = 1;

    @Column(name = "current_day", nullable = false)
    @Builder.Default
    private Integer currentDay = 1;

    @Column(name = "total_coins", nullable = false)
    @Builder.Default
    private Integer totalCoins = 0;

    @Column(name = "experience_points", nullable = false)
    @Builder.Default
    private Integer experiencePoints = 0;

    @Column(name = "streak_days")
    @Builder.Default
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
    }

    /**
     * 경험치 추가
     */
    public void addExperience(Integer exp) {
        this.experiencePoints += exp;
    }

    /**
     * 레벨업 체크 및 실행
     */
    public boolean checkLevelUp() {
        int requiredExp = currentLevel * 100; // 레벨당 필요 경험치
        if (experiencePoints >= requiredExp) {
            currentLevel++;
            experiencePoints -= requiredExp;
            return true;
        }
        return false;
    }

    /**
     * 스트릭 업데이트
     */
    public void updateStreak() {
        LocalDate today = LocalDate.now();
        if (lastLearningDate == null) {
            streakDays = 1;
        } else if (lastLearningDate.equals(today.minusDays(1))) {
            streakDays++;
        } else if (!lastLearningDate.equals(today)) {
            streakDays = 1;
        }
        lastLearningDate = today;
    }

    /**
     * 다음 레벨까지 필요한 경험치
     */
    public Integer getExpToNextLevel() {
        return (currentLevel * 100) - experiencePoints;
    }

    /**
     * 현재 레벨 진행률 (%)
     */
    public Double getLevelProgress() {
        int requiredExp = currentLevel * 100;
        return (double) experiencePoints / requiredExp * 100;
    }

    /**
     * 총 학습 일수
     */
    public Integer getTotalLearningDays() {
        return streakDays;
    }

    /**
     * 오늘 학습했는지 확인
     */
    public Boolean isLearnedToday() {
        return lastLearningDate != null && lastLearningDate.equals(LocalDate.now());
    }

    /**
     * 연속 학습 중인지 확인
     */
    public Boolean isOnStreak() {
        return streakDays > 0 && (lastLearningDate == null || 
               lastLearningDate.equals(LocalDate.now()) || 
               lastLearningDate.equals(LocalDate.now().minusDays(1)));
    }

    /**
     * 레벨별 보상 코인 계산
     */
    public Integer calculateLevelReward() {
        return currentLevel * 10; // 레벨당 10코인
    }

    /**
     * 스트릭 보너스 코인 계산
     */
    public Integer calculateStreakBonus() {
        if (streakDays >= 7) {
            return streakDays / 7 * 5; // 주당 5코인
        }
        return 0;
    }
}
