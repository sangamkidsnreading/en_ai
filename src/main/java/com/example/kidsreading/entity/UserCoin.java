package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_coins")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Builder.Default
    @Column(name = "words_coins")
    private Integer wordsCoins = 0;

    @Builder.Default
    @Column(name = "sentence_coins")
    private Integer sentenceCoins = 0;

    @Builder.Default
    @Column(name = "streak_bonus")
    private Integer streakBonus = 0;

    @Builder.Default
    @Column(name = "total_daily_coins")
    private Integer totalDailyCoins = 0;

    @Builder.Default
    @Column(name = "coins_earned")
    private Integer coinsEarned = 0;

    @Builder.Default
    @Column(name = "daily_coins")
    private Integer dailyCoins = 0;

    @Builder.Default
    @Column(name = "total_coins")
    private Integer totalCoins = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (date == null) {
            date = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public void addWordCoins(Integer coins) {
        this.wordsCoins += coins;
        this.coinsEarned += coins;
        this.totalCoins += coins;
        this.dailyCoins += coins;
        this.totalDailyCoins += coins;
    }

    public void addSentenceCoins(Integer coins) {
        this.sentenceCoins += coins;
        this.coinsEarned += coins;
        this.totalCoins += coins;
        this.dailyCoins += coins;
        this.totalDailyCoins += coins;
    }

    public void addStreakBonus(Integer bonus) {
        this.streakBonus += bonus;
        this.coinsEarned += bonus;
        this.totalCoins += bonus;
        this.dailyCoins += bonus;
        this.totalDailyCoins += bonus;
    }
}