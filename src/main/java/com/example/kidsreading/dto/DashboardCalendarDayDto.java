package com.example.kidsreading.dto;

import java.time.LocalDate;

public class DashboardCalendarDayDto {
    private LocalDate date;
    private boolean completed;
    private int wordCount;
    private int sentenceCount;
    private int coinCount;

    public DashboardCalendarDayDto() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }
    public int getSentenceCount() { return sentenceCount; }
    public void setSentenceCount(int sentenceCount) { this.sentenceCount = sentenceCount; }
    public int getCoinCount() { return coinCount; }
    public void setCoinCount(int coinCount) { this.coinCount = coinCount; }
} 