package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakDto {
    private Long userId;
    private String email;
    private Integer currentStreak;
    private Integer maxStreak;
    private LocalDate lastLearningDate;
    private LocalDate streakStartDate;
    private boolean isLearnedToday;
    private boolean isOnStreak;
    private int streakBonus;
    private double streakProgress;
    private int daysToNextGoal;
    private int currentGoal;
    private String streakMessage;
    private String nextGoalMessage;
} 