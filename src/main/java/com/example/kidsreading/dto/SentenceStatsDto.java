package com.example.kidsreading.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceStatsDto {
    private Long sentenceId;
    private String text;
    private String translation;
    private Integer level;
    private Integer day;
    private Integer totalLearned; // 총 학습 횟수
    private Integer uniqueUsers; // 학습한 고유 사용자 수
    private Integer recordingCount; // 녹음한 사용자 수
    private Double averageLearnTime; // 평균 학습 시간
    private Double difficultyScore; // 난이도 점수
}