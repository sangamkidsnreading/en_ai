package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWordProgressDto {
    private Long id;
    private Long userId;
    private Long wordId;
    private String wordText; // 조회 시 편의를 위해
    private String wordMeaning; // 조회 시 편의를 위해
    private Boolean isLearned;
    private Boolean isFavorite;
    private Integer learnCount;
    private LocalDateTime firstLearnedAt;
    private LocalDateTime lastLearnedAt;
    private LocalDateTime createdAt;

    private Integer correctCount;
    private Integer incorrectCount;
    private LocalDateTime lastStudied;
    private Boolean isCompleted;
    private String userEmail;

}
