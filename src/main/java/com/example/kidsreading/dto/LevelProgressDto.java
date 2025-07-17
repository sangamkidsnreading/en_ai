package com.example.kidsreading.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelProgressDto {
    private int currentLevel;
    private int levelProgress; // 0~100
    private int wordsToNextLevel;
    private int sentencesToNextLevel;
}
