package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSentenceProgressDto {
    private Long id;
    private Long userId;
    private Long sentenceId;
    private String sentenceText; // 조회 시 편의를 위해
    private String sentenceTranslation; // 조회 시 편의를 위해
    private Boolean isLearned;
    private Boolean hasRecording;
    private String recordingUrl;
    private Integer learnCount;
    private LocalDateTime firstLearnedAt;
    private LocalDateTime lastLearnedAt;
    private LocalDateTime createdAt;

    private Integer correctCount;
    private Integer incorrectCount;
    private LocalDateTime lastStudied;
    private Boolean isCompleted;

}