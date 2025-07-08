package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsDto {
    private LocalDate date;
    private Integer newUsersCount;
    private Integer activeUsersCount;
    private Integer wordsLearnedCount;
    private Integer sentencesLearnedCount;
    private Integer totalCoinsEarned;
    private Double averageSessionTime; // 평균 학습 시간 (분)

    private long completedWords;
    private long completedSentences;
    private long activeUsers;

}