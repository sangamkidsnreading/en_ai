package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressSummaryDto {
    private Long userId;
    private String username;
    private String name;
    private Integer currentLevel;
    private Integer totalWordsLearned;
    private Integer totalSentencesLearned;
    private Integer totalCoinsEarned;
    private Integer streakDays;
    private LocalDateTime lastLogin;
    private LocalDateTime lastLearningDate;
    private Double completionRate; // 전체 완료율 (%)

    private long completedWords;
    private long completedSentences;
    private int totalCoins;
    private int currentStreak;

}
