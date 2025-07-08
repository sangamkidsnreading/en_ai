package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_words_per_day")
    private Integer maxWordsPerDay;

    @Column(name = "max_sentences_per_day")
    private Integer maxSentencesPerDay;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "enable_audio")
    private Boolean enableAudio;

    @Column(name = "enable_feedback")
    private Boolean enableFeedback;

    @Column(name = "audio_speed")
    private Double audioSpeed;

    @Column(name = "voice_speed")
    private Integer voiceSpeed;

    @Column(name = "repeat_count")
    private Integer repeatCount;

    @Column(name = "word_coin")
    private Integer wordCoin;

    @Column(name = "sentence_coin")
    private Integer sentenceCoin;

    @Column(name = "streak_bonus")
    private Integer streakBonus;

    @Column(name = "level_up_coin")
    private Integer levelUpCoin;

    @Column(name = "max_level")
    private Integer maxLevel;

    @Column(name = "daily_word_goal")
    private Integer dailyWordGoal;

    @Column(name = "daily_sentence_goal")
    private Integer dailySentenceGoal;

    @Column(name = "voice_enabled")
    private Boolean voiceEnabled;

    @Column(name = "voice_language")
    private String voiceLanguage;

    @Column(name = "word_coins")
    private Integer wordCoins;

    @Column(name = "sentence_coins")
    private Integer sentenceCoins;

    @Column(name = "daily_goal")
    private Integer dailyGoal;

    @Column(name = "sound_effects")
    private Boolean soundEffects;

    @Column(name = "auto_play")
    private Boolean autoPlay;

    @Column(name = "dark_mode")
    private Boolean darkMode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}