// ========== LearningAnalyticsDto.java ==========
package com.example.kidsreading.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningAnalyticsDto {
    private Map<String, Integer> dailyActiveUsers; // 일별 활성 사용자
    private Map<String, Integer> levelDistribution; // 레벨별 사용자 분포
    private Map<String, Double> completionRates; // 레벨별 완료율
    private List<WordStatsDto> topDifficultWords; // 어려운 단어 Top 10
    private List<SentenceStatsDto> topDifficultSentences; // 어려운 문장 Top 10
    private Map<String, Integer> monthlyProgress; // 월별 학습 진도
}