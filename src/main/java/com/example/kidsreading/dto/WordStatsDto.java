package com.example.kidsreading.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordStatsDto {

    private long totalWords;
    private long completedWords;
    private long activeWords;
    private double averageAccuracy;
    private int mostStudiedLevel;

    private Long wordId;
    private String text;
    private String meaning;
    private Integer level;
    private Integer day;
    private Integer totalLearned; // 총 학습 횟수
    private Integer uniqueUsers; // 학습한 고유 사용자 수
    private Double averageLearnTime; // 평균 학습 시간
    private Double difficultyScore; // 난이도 점수 (재학습 횟수 기반)
}