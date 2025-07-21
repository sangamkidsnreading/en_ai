package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeDto {
    private Long id;
    private Long userId;
    private Long badgeId;
    private String badgeName;
    private String badgeIcon;
    private String badgeDescription;
    private LocalDateTime earnedAt;
    private Boolean isDisplayed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 