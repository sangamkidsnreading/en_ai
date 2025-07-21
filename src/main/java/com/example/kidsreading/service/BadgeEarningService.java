package com.example.kidsreading.service;

import com.example.kidsreading.entity.BadgeSettings;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.BadgeSettingsRepository;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.UserStreakRepository;
import com.example.kidsreading.service.UserAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeEarningService {

    private final UserBadgeService userBadgeService;
    private final BadgeSettingsRepository badgeSettingsRepository;
    private final UserRepository userRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserAttendanceService userAttendanceService;

    /**
     * 출석 뱃지 체크 및 획득
     */
    public void checkAttendanceBadges(Long userId, int attendanceCount) {
        List<BadgeSettings> attendanceBadges = badgeSettingsRepository.findByAttendanceCountIsNotNullAndIsActiveTrueOrderByAttendanceCountAsc();
        
        for (BadgeSettings badge : attendanceBadges) {
            if (attendanceCount >= badge.getAttendanceCount() && 
                !userBadgeService.hasUserEarnedBadge(userId, badge.getId())) {
                userBadgeService.earnBadge(userId, badge.getId());
            }
        }
    }

    /**
     * 연속 출석 뱃지 체크 및 획득
     */
    public void checkStreakBadges(Long userId, int streakCount) {
        List<BadgeSettings> streakBadges = badgeSettingsRepository.findByStreakCountIsNotNullAndIsActiveTrueOrderByStreakCountAsc();
        
        for (BadgeSettings badge : streakBadges) {
            if (streakCount >= badge.getStreakCount() && 
                !userBadgeService.hasUserEarnedBadge(userId, badge.getId())) {
                userBadgeService.earnBadge(userId, badge.getId());
            }
        }
    }

    /**
     * 단어 학습 뱃지 체크 및 획득
     */
    public void checkWordsBadges(Long userId, int wordsCount) {
        List<BadgeSettings> wordsBadges = badgeSettingsRepository.findByWordsCountIsNotNullAndIsActiveTrueOrderByWordsCountAsc();
        
        for (BadgeSettings badge : wordsBadges) {
            if (wordsCount >= badge.getWordsCount() && 
                !userBadgeService.hasUserEarnedBadge(userId, badge.getId())) {
                userBadgeService.earnBadge(userId, badge.getId());
            }
        }
    }

    /**
     * 문장 학습 뱃지 체크 및 획득
     */
    public void checkSentencesBadges(Long userId, int sentencesCount) {
        List<BadgeSettings> sentencesBadges = badgeSettingsRepository.findBySentencesCountIsNotNullAndIsActiveTrueOrderBySentencesCountAsc();
        
        for (BadgeSettings badge : sentencesBadges) {
            if (sentencesCount >= badge.getSentencesCount() && 
                !userBadgeService.hasUserEarnedBadge(userId, badge.getId())) {
                userBadgeService.earnBadge(userId, badge.getId());
            }
        }
    }

    /**
     * 단어 복습 뱃지 체크 및 획득
     */
    public void checkWordReviewBadges(Long userId, int wordReviewCount) {
        List<BadgeSettings> wordReviewBadges = badgeSettingsRepository.findByWordReviewCountIsNotNullAndIsActiveTrueOrderByWordReviewCountAsc();
        
        for (BadgeSettings badge : wordReviewBadges) {
            if (wordReviewCount >= badge.getWordReviewCount() && 
                !userBadgeService.hasUserEarnedBadge(userId, badge.getId())) {
                userBadgeService.earnBadge(userId, badge.getId());
            }
        }
    }

    /**
     * 문장 복습 뱃지 체크 및 획득
     */
    public void checkSentenceReviewBadges(Long userId, int sentenceReviewCount) {
        List<BadgeSettings> sentenceReviewBadges = badgeSettingsRepository.findBySentenceReviewCountIsNotNullAndIsActiveTrueOrderBySentenceReviewCountAsc();
        
        for (BadgeSettings badge : sentenceReviewBadges) {
            if (sentenceReviewCount >= badge.getSentenceReviewCount() && 
                !userBadgeService.hasUserEarnedBadge(userId, badge.getId())) {
                userBadgeService.earnBadge(userId, badge.getId());
            }
        }
    }

    /**
     * 모든 뱃지 조건 체크 및 획득
     */
    public void checkAllBadges(Long userId, int attendanceCount, int streakCount, 
                              int wordsCount, int sentencesCount, 
                              int wordReviewCount, int sentenceReviewCount) {
        checkAttendanceBadges(userId, attendanceCount);
        checkStreakBadges(userId, streakCount);
        checkWordsBadges(userId, wordsCount);
        checkSentencesBadges(userId, sentencesCount);
        checkWordReviewBadges(userId, wordReviewCount);
        checkSentenceReviewBadges(userId, sentenceReviewCount);
    }

    /**
     * 사용자 통계 기반 뱃지 체크 (실제 데이터 사용)
     */
    public void checkBadgesByUserStats(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // 실제 사용자 통계 데이터 조회
        int attendanceCount = getAttendanceCount(userId);
        int streakCount = getStreakCount(userId);
        int wordsCount = getCompletedWordsCount(userId);
        int sentencesCount = getCompletedSentencesCount(userId);
        int wordReviewCount = getWordReviewCount(userId);
        int sentenceReviewCount = getSentenceReviewCount(userId);

        checkAllBadges(userId, attendanceCount, streakCount, wordsCount, 
                      sentencesCount, wordReviewCount, sentenceReviewCount);
    }

    /**
     * 출석 횟수 계산 (user_attendance 테이블 기반)
     */
    private int getAttendanceCount(Long userId) {
        Long attendanceDays = userAttendanceService.getTotalAttendanceDays(userId);
        return attendanceDays != null ? attendanceDays.intValue() : 0;
    }

    /**
     * 연속 출석 횟수 조회
     */
    private int getStreakCount(Long userId) {
        return userStreakRepository.findByUserId(userId)
                .map(streak -> streak.getCurrentStreak())
                .orElse(0);
    }

    /**
     * 완료된 단어 수 조회
     */
    private int getCompletedWordsCount(Long userId) {
        return userWordProgressRepository.countTotalCompletedWords(userId);
    }

    /**
     * 완료된 문장 수 조회
     */
    private int getCompletedSentencesCount(Long userId) {
        return (int) userSentenceProgressRepository.countByUserIdAndIsCompletedTrue(userId);
    }

    /**
     * 단어 복습 횟수 조회 (learn_count 합계)
     */
    private int getWordReviewCount(Long userId) {
        return userWordProgressRepository.findByUserId(userId).stream()
                .mapToInt(progress -> progress.getLearnCount())
                .sum();
    }

    /**
     * 문장 복습 횟수 조회 (learn_count 합계)
     */
    private int getSentenceReviewCount(Long userId) {
        return userSentenceProgressRepository.findByUserId(userId).stream()
                .mapToInt(progress -> progress.getLearnCount())
                .sum();
    }

    /**
     * 단어 학습 완료 시 뱃지 체크
     */
    public void checkBadgesOnWordCompletion(Long userId) {
        checkBadgesByUserStats(userId);
    }

    /**
     * 문장 학습 완료 시 뱃지 체크
     */
    public void checkBadgesOnSentenceCompletion(Long userId) {
        checkBadgesByUserStats(userId);
    }

    /**
     * 연속 학습일 업데이트 시 뱃지 체크
     */
    public void checkBadgesOnStreakUpdate(Long userId) {
        checkBadgesByUserStats(userId);
    }

    /**
     * 사용자 통계 요약 조회
     */
    public UserBadgeStatsDto getUserBadgeStats(Long userId) {
        return UserBadgeStatsDto.builder()
                .userId(userId)
                .attendanceCount(getAttendanceCount(userId))
                .streakCount(getStreakCount(userId))
                .completedWordsCount(getCompletedWordsCount(userId))
                .completedSentencesCount(getCompletedSentencesCount(userId))
                .wordReviewCount(getWordReviewCount(userId))
                .sentenceReviewCount(getSentenceReviewCount(userId))
                .build();
    }

    /**
     * 뱃지 통계 DTO
     */
    public static class UserBadgeStatsDto {
        private Long userId;
        private int attendanceCount;
        private int streakCount;
        private int completedWordsCount;
        private int completedSentencesCount;
        private int wordReviewCount;
        private int sentenceReviewCount;

        // Builder, Getter, Setter 생략
        public static UserBadgeStatsDtoBuilder builder() {
            return new UserBadgeStatsDtoBuilder();
        }

        public static class UserBadgeStatsDtoBuilder {
            private UserBadgeStatsDto dto = new UserBadgeStatsDto();

            public UserBadgeStatsDtoBuilder userId(Long userId) {
                dto.userId = userId;
                return this;
            }

            public UserBadgeStatsDtoBuilder attendanceCount(int attendanceCount) {
                dto.attendanceCount = attendanceCount;
                return this;
            }

            public UserBadgeStatsDtoBuilder streakCount(int streakCount) {
                dto.streakCount = streakCount;
                return this;
            }

            public UserBadgeStatsDtoBuilder completedWordsCount(int completedWordsCount) {
                dto.completedWordsCount = completedWordsCount;
                return this;
            }

            public UserBadgeStatsDtoBuilder completedSentencesCount(int completedSentencesCount) {
                dto.completedSentencesCount = completedSentencesCount;
                return this;
            }

            public UserBadgeStatsDtoBuilder wordReviewCount(int wordReviewCount) {
                dto.wordReviewCount = wordReviewCount;
                return this;
            }

            public UserBadgeStatsDtoBuilder sentenceReviewCount(int sentenceReviewCount) {
                dto.sentenceReviewCount = sentenceReviewCount;
                return this;
            }

            public UserBadgeStatsDto build() {
                return dto;
            }
        }

        // Getters
        public Long getUserId() { return userId; }
        public int getAttendanceCount() { return attendanceCount; }
        public int getStreakCount() { return streakCount; }
        public int getCompletedWordsCount() { return completedWordsCount; }
        public int getCompletedSentencesCount() { return completedSentencesCount; }
        public int getWordReviewCount() { return wordReviewCount; }
        public int getSentenceReviewCount() { return sentenceReviewCount; }
    }
} 