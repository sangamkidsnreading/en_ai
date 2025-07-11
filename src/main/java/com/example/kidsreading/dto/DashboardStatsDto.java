package com.example.kidsreading.dto;

public class DashboardStatsDto {
    private int wordCount;
    private int sentenceCount;
    private int coinCount;
    private int streak;

    public DashboardStatsDto() {}

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }
    public int getSentenceCount() { return sentenceCount; }
    public void setSentenceCount(int sentenceCount) { this.sentenceCount = sentenceCount; }
    public int getCoinCount() { return coinCount; }
    public void setCoinCount(int coinCount) { this.coinCount = coinCount; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
} 