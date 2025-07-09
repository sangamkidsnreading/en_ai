package com.example.kidsreading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSettingsDto {
    private Long id;
    private Integer maxWordsPerDay;
    private Integer maxSentencesPerDay;
    private Integer difficultyLevel;
    private Boolean enableAudio;
    private Boolean enableFeedback;
    private Double audioSpeed;
    private Integer voiceSpeed;
    private Integer repeatCount;
    private Integer wordCoin;
    private Integer sentenceCoin;
    private Integer streakBonus;
    private Integer levelUpCoin;
    private Integer maxLevel;
    private Integer dailyWordGoal;
    private Integer dailySentenceGoal;
    private Boolean voiceEnabled;
    private String voiceLanguage;
    private Integer wordCoins;
    private Integer sentenceCoins;
    private Integer dailyGoal;
    private Boolean soundEffects;
    private Boolean autoPlay;
    private Boolean darkMode;
}