package com.example.kidsreading.dto;

import java.util.List;

public class MainContentStatsDto {
    private int todayWords;
    private int totalWords;
    private int todaySentences;
    private int totalSentences;
    private List<Long> completedWordIds;
    private List<Long> completedSentenceIds;

    public MainContentStatsDto() {}

    public int getTodayWords() {
        return todayWords;
    }
    public void setTodayWords(int todayWords) {
        this.todayWords = todayWords;
    }
    public int getTotalWords() {
        return totalWords;
    }
    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }
    public int getTodaySentences() {
        return todaySentences;
    }
    public void setTodaySentences(int todaySentences) {
        this.todaySentences = todaySentences;
    }
    public int getTotalSentences() {
        return totalSentences;
    }
    public void setTotalSentences(int totalSentences) {
        this.totalSentences = totalSentences;
    }

    public List<Long> getCompletedWordIds() { return completedWordIds; }
    public void setCompletedWordIds(List<Long> completedWordIds) { this.completedWordIds = completedWordIds; }
    public List<Long> getCompletedSentenceIds() { return completedSentenceIds; }
    public void setCompletedSentenceIds(List<Long> completedSentenceIds) { this.completedSentenceIds = completedSentenceIds; }
} 