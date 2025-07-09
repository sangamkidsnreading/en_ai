package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "session_date"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSessions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "words_learned", nullable = false)
    @Builder.Default
    private Integer wordsLearned = 0;

    @Column(name = "sentences_learned", nullable = false)
    @Builder.Default
    private Integer sentencesLearned = 0;

    @Column(name = "coins_earned", nullable = false)
    @Builder.Default
    private Integer coinsEarned = 0;

    @Column(name = "session_duration_minutes")
    @Builder.Default
    private Integer sessionDurationMinutes = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sessionDate == null) {
            sessionDate = LocalDate.now();
        }
    }

    // 비즈니스 메서드들

    /**
     * 단어 학습 추가
     */
    public void addWordLearned(Integer coins) {
        this.wordsLearned++;
        this.coinsEarned += coins;
    }

    /**
     * 문장 학습 추가
     */
    public void addSentenceLearned(Integer coins) {
        this.sentencesLearned++;
        this.coinsEarned += coins;
    }

    /**
     * 세션 시간 업데이트
     */
    public void updateSessionDuration(Integer additionalMinutes) {
        this.sessionDurationMinutes += additionalMinutes;
    }

    /**
     * 오늘 세션인지 확인
     */
    public boolean isTodaySession() {
        return sessionDate.equals(LocalDate.now());
    }

    /**
     * 세션 요약 정보 반환
     */
    public String getSessionSummary() {
        return String.format("학습일: %s, 단어: %d개, 문장: %d개, 코인: %d개, 시간: %d분",
                sessionDate, wordsLearned, sentencesLearned, coinsEarned, sessionDurationMinutes);
    }
}