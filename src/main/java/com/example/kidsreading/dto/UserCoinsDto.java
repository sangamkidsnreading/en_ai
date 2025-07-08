
package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoinsDto {
    private Long id;
    private String userId;
    private String date;
    private Integer wordsCoins;
    private Integer sentenceCoins;
    private Integer streakBonus;
    private Integer totalDailyCoins;
    private Integer coinsEarned;
    private Integer dailyCoins;
    private Integer totalCoins;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}
