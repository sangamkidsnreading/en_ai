package com.example.kidsreading.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    private long totalUsers;
    private long totalWords;
    private long totalSentences;
    private long activeUsers;
    private long todayActiveUsers;
    private long weeklyActiveUsers;
    private long monthlyActiveUsers;
    private int totalLevels;
}