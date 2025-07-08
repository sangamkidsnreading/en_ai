package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sentence_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sentence_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSentenceProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sentence_id", nullable = false)
    private Long sentenceId;

    @Column(name = "is_learned", nullable = false)
    private Boolean isLearned = false;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "has_recording", nullable = false)
    private Boolean hasRecording = false;

    @Column(name = "recording_url")
    private String recordingUrl;

    @Column(name = "learn_count", nullable = false)
    private Integer learnCount = 0;

    @Column(name = "first_learned_at")
    private LocalDateTime firstLearnedAt;

    @Column(name = "last_learned_at")
    private LocalDateTime lastLearnedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentence_id", insertable = false, updatable = false)
    private Sentence sentence;

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