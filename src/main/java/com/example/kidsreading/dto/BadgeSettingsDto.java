package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeSettingsDto {
    private Long id;
    private String badgeName;
    private String badgeIcon;
    private String badgeDescription;
    private Integer attendanceCount;
    private Integer streakCount;
    private Integer wordsCount;
    private Integer sentencesCount;
    private Integer wordReviewCount;
    private Integer sentenceReviewCount;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 