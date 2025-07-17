package com.example.kidsreading.dto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RankingDto {
    private int rank;
    private String name;
    private int wordsLearned;
    private int sentencesLearned;
    private String badge; // 예: "오늘"
}
