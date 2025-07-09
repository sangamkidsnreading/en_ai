
package com.example.kidsreading.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDto {
    private long totalUsers;
    private long totalWords;
    private long totalSentences;
    private long activeUsers;
    private long completedLearnings;
    private double averageProgress;
}
