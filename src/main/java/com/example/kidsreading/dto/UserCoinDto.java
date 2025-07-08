package com.example.kidsreading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoinDto {
    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer wordsCoins;
    private Integer sentenceCoins;
    private Integer streakBonus;
    private Integer totalDailyCoins;
    private Integer coinsEarned;
    private Integer dailyCoins;
    private Integer totalCoins;
    private LocalDateTime createdAt;
}