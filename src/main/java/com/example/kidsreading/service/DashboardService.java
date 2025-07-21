package com.example.kidsreading.service;

import com.example.kidsreading.dto.DashboardDto;
import com.example.kidsreading.dto.TodayProgressDto;
import com.example.kidsreading.dto.RankingDto;
import com.example.kidsreading.dto.LevelProgressDto;
import com.example.kidsreading.entity.UserLevels;
import com.example.kidsreading.repository.UserLevelsRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.WordRepository;
import com.example.kidsreading.repository.SentenceRepository;
import com.example.kidsreading.entity.UserWordProgress;
import com.example.kidsreading.entity.UserSentenceProgress;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.entity.BadgeSettings;
import com.example.kidsreading.repository.BadgeSettingsRepository;
import com.example.kidsreading.entity.UserBadge;
import com.example.kidsreading.repository.UserBadgeRepository;
import com.example.kidsreading.dto.BadgeSettingsDto;
import com.example.kidsreading.service.BadgeSettingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardService {

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private UserSentenceProgressRepository userSentenceProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private SentenceRepository sentenceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserLevelsRepository userLevelsRepository;

    @Autowired
    private BadgeSettingsRepository badgeSettingsRepository;
    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private BadgeSettingsService badgeSettingsService;

    public List<BadgeSettingsDto> getAllBadgeSettings() {
        return badgeSettingsService.getAllBadgeSettings();
    }

    public List<BadgeSettingsDto> getActiveBadgeSettings() {
        return badgeSettingsService.getActiveBadgeSettings();
    }

    /**
     * 대시보드 메인 데이터 조회
     */
    public DashboardDto getDashboardData(Long userId) {
        try {
            // UserLevels에서 사용자 레벨 정보 조회
            UserLevels userLevels = userLevelsRepository.findByUserId(userId)
                    .orElseGet(() -> createDefaultUserLevels(userId));

            // 학습 완료된 단어/문장 수 조회
            long wordsLearned = userWordProgressRepository.countByUserIdAndIsLearnedTrue(userId);
            long sentencesLearned = userSentenceProgressRepository.countByUserIdAndIsLearnedTrue(userId);

            // 총 코인 (UserLevels에서 조회)
            int totalCoins = userLevels.getTotalCoins();

            // 연속 학습일 (UserLevels에서 조회)
            int streakDays = userLevels.getStreakDays();

            // 현재 레벨 (UserLevels에서 조회)
            int currentLevel = userLevels.getCurrentLevel();

            // 레벨 진행도 계산 (UserService 사용)
            LevelProgressDto levelProgress = userService.getLevelProgressForUser(userId);
            int levelProgressPercent = levelProgress.getLevelProgress();

            // 다음 레벨까지 필요한 단어/문장 수
            int wordsToNextLevel = levelProgress.getWordsToNextLevel();
            int sentencesToNextLevel = levelProgress.getSentencesToNextLevel();

            // 완료율 계산
            double completionRate = calculateCompletionRate(wordsLearned, sentencesLearned);

            // 전체 단어/문장 수
            long totalWords = wordRepository.countByIsActiveTrue();
            long totalSentences = sentenceRepository.countByIsActiveTrue();

            // 일일 목표 (기본값)
            int dailyWordGoal = 10;
            int dailySentenceGoal = 5;

            // 일일 진행도 (오늘 학습한 단어/문장 수)
            long dailyWordProgress = userWordProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                    userId, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0), LocalDateTime.now());
            long dailySentenceProgress = userSentenceProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                    userId, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0), LocalDateTime.now());

            // 이전 데이터 (어제 학습한 단어/문장 수)
            long previousWordsLearned = userWordProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                    userId, LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0), 
                    LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59));
            long previousSentencesLearned = userSentenceProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                    userId, LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0), 
                    LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59));

            return DashboardDto.builder()
                    .wordsLearned((int) wordsLearned)
                    .sentencesLearned((int) sentencesLearned)
                    .totalCoins(totalCoins)
                    .streakDays(streakDays)
                    .currentLevel(currentLevel)
                    .levelProgress(levelProgressPercent)
                    .wordsToNextLevel(wordsToNextLevel)
                    .completionRate(completionRate)
                    .totalWords((int) totalWords)
                    .totalSentences((int) totalSentences)
                    .dailyWordGoal(dailyWordGoal)
                    .dailySentenceGoal(dailySentenceGoal)
                    .dailyWordProgress((int) dailyWordProgress)
                    .dailySentenceProgress((int) dailySentenceProgress)
                    .previousWordsLearned((int) previousWordsLearned)
                    .previousSentencesLearned((int) previousSentencesLearned)
                    .previousTotalCoins(totalCoins) // 간단히 현재 코인으로 설정
                    .build();

        } catch (Exception e) {
            // 에러 발생 시 기본 데이터 반환
            return DashboardDto.builder()
                    .wordsLearned(0)
                    .sentencesLearned(0)
                    .totalCoins(100)
                    .streakDays(1)
                    .currentLevel(1)
                    .levelProgress(0)
                    .wordsToNextLevel(100)
                    .completionRate(0.0)
                    .totalWords(0)
                    .totalSentences(0)
                    .dailyWordGoal(10)
                    .dailySentenceGoal(5)
                    .dailyWordProgress(0)
                    .dailySentenceProgress(0)
                    .previousWordsLearned(0)
                    .previousSentencesLearned(0)
                    .previousTotalCoins(0)
                    .build();
        }
    }

    /**
     * 기본 UserLevels 생성
     */
    private UserLevels createDefaultUserLevels(Long userId) {
        UserLevels userLevels = UserLevels.builder()
                .userId(userId)
                .currentLevel(1)
                .currentDay(1)
                .totalCoins(0)
                .experiencePoints(0)
                .streakDays(0)
                .build();
        return userLevelsRepository.save(userLevels);
    }

    /**
     * 월간 달력 데이터 조회
     */
    public Map<String, Object> getCalendarData(Long userId, Integer year, Integer month) {
        try {
            Map<String, Object> calendarData = new HashMap<>();
            Map<String, Object> daysData = new HashMap<>();

            // 해당 월의 첫날과 마지막날 계산
            LocalDate firstDay = LocalDate.of(year, month, 1);
            LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

            // 각 날짜별 학습 데이터 조회
            for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                String dateKey = date.toString();

                // 해당 날짜에 학습 완료된 단어 수
                long completedWords = userWordProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                        userId,
                        date.atStartOfDay(),
                        date.atTime(23, 59, 59)
                );

                // 해당 날짜에 학습 완료된 문장 수
                long completedSentences = userSentenceProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                        userId,
                        date.atStartOfDay(),
                        date.atTime(23, 59, 59)
                );

                // 코인 집계 (단어 10, 문장 20)
                long coinsEarned = completedWords * 10 + completedSentences * 20;

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateKey);
                dayData.put("status", (completedWords > 0 || completedSentences > 0) ? "completed" : "not-started");
                dayData.put("completedWords", completedWords);
                dayData.put("completedSentences", completedSentences);
                dayData.put("coinsEarned", coinsEarned);

                daysData.put(dateKey, dayData);
            }
            
            calendarData.put("calendarData", daysData);
            calendarData.put("currentMonth", month);
            calendarData.put("currentYear", year);
            
            return calendarData;
        } catch (Exception e) {
            Map<String, Object> defaultCalendar = new HashMap<>();
            defaultCalendar.put("calendarData", new HashMap<>());
            defaultCalendar.put("currentMonth", month);
            defaultCalendar.put("currentYear", year);
            return defaultCalendar;
        }
    }

    /**
     * 뱃지 정보 조회
     */
    public Map<String, Object> getBadgesData(Long userId) {
        try {
            Map<String, Object> badgesData = new HashMap<>();
            List<Map<String, Object>> badges = new ArrayList<>();

            // 실제 DB에서 is_active=true인 뱃지만 조회
            List<BadgeSettings> activeBadges = badgeSettingsRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
            List<UserBadge> userBadges = userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
            Set<Long> earnedBadgeIds = userBadges.stream().map(ub -> ub.getBadge().getId()).collect(Collectors.toSet());
            Map<Long, LocalDateTime> earnedDates = userBadges.stream().collect(Collectors.toMap(ub -> ub.getBadge().getId(), UserBadge::getEarnedAt, (a, b) -> a));

            for (BadgeSettings badge : activeBadges) {
                Map<String, Object> badgeMap = new HashMap<>();
                badgeMap.put("id", badge.getId());
                badgeMap.put("name", badge.getBadgeName());
                badgeMap.put("icon", badge.getBadgeIcon());
                badgeMap.put("description", badge.getBadgeDescription());
                badgeMap.put("attendanceCount", badge.getAttendanceCount());
                badgeMap.put("streakCount", badge.getStreakCount());
                badgeMap.put("wordsCount", badge.getWordsCount());
                badgeMap.put("sentencesCount", badge.getSentencesCount());
                badgeMap.put("wordReviewCount", badge.getWordReviewCount());
                badgeMap.put("sentenceReviewCount", badge.getSentenceReviewCount());
                boolean isEarned = earnedBadgeIds.contains(badge.getId());
                badgeMap.put("isEarned", isEarned);
                badgeMap.put("earnedDate", isEarned ? earnedDates.get(badge.getId()) : null);
                badges.add(badgeMap);
            }

            badgesData.put("badges", badges);
            badgesData.put("totalBadges", badges.size());
            badgesData.put("earnedBadges", earnedBadgeIds.size());

            return badgesData;
        } catch (Exception e) {
            Map<String, Object> defaultBadges = new HashMap<>();
            defaultBadges.put("badges", new ArrayList<>());
            defaultBadges.put("totalBadges", 0);
            defaultBadges.put("earnedBadges", 0);
            return defaultBadges;
        }
    }

    /**
     * 랭킹 정보 조회
     */
    public Map<String, Object> getRankingsData(Long userId) {
        try {
            Map<String, Object> rankingsData = new HashMap<>();
            List<Map<String, Object>> rankings = new ArrayList<>();
            
            // 기본 랭킹 데이터 (실제로는 DB에서 조회해야 함)
            String[] names = {"김학습자", "관리자"};
            int[] wordsLearned = {10, 8};
            int[] sentencesLearned = {8, 6};
            int[] coins = {150, 120};
            
            for (int i = 0; i < names.length; i++) {
                Map<String, Object> ranking = new HashMap<>();
                ranking.put("rank", i + 1);
                ranking.put("username", names[i].toLowerCase().replace(" ", ""));
                ranking.put("name", names[i]);
                ranking.put("wordsLearned", wordsLearned[i]);
                ranking.put("sentencesLearned", sentencesLearned[i]);
                ranking.put("totalCoins", coins[i]);
                ranking.put("badge", "오늘");
                rankings.add(ranking);
            }
            
            rankingsData.put("rankings", rankings);
            rankingsData.put("userRank", 1);
            
            return rankingsData;
        } catch (Exception e) {
            Map<String, Object> defaultRankings = new HashMap<>();
            defaultRankings.put("rankings", new ArrayList<>());
            defaultRankings.put("userRank", 0);
            return defaultRankings;
        }
    }

    /**
     * 레벨 진행도 조회
     */
    public LevelProgressDto getLevelProgress(Long userId) {
        try {
            // UserService의 메서드를 사용하여 레벨 설정을 고려한 진행도 계산
            LevelProgressDto levelProgress = userService.getLevelProgressForUser(userId);
            return levelProgress;
        } catch (Exception e) {
            // 에러 발생 시 기본값 반환
            return LevelProgressDto.builder()
                .currentLevel(1)
                .levelProgress(0)
                .wordsToNextLevel(100)
                .sentencesToNextLevel(50)
                .build();
        }
    }

    /**
     * 일일 목표 진행도 조회
     */
    public Map<String, Object> getDailyGoals(Long userId) {
        Map<String, Object> goals = new HashMap<>();
        goals.put("dailyWordGoal", 10);
        goals.put("dailySentenceGoal", 5);
        goals.put("dailyWordProgress", 0);
        goals.put("dailySentenceProgress", 0);
        return goals;
    }

    /**
     * 특정 날짜의 학습 통계 조회
     */
    public Map<String, Object> getStatsByDate(Long userId, String date) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDate targetDate = LocalDate.parse(date);
            
            // 해당 날짜에 완료된 단어 수 조회
            long completedWords = userWordProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                    userId,
                    targetDate.atStartOfDay(),
                    targetDate.atTime(23, 59, 59)
            );
            
            // 해당 날짜에 완료된 문장 수 조회
            long completedSentences = userSentenceProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(
                    userId,
                    targetDate.atStartOfDay(),
                    targetDate.atTime(23, 59, 59)
            );
            
            // 해당 날짜에 획득한 코인 수 조회 (기본값 사용)
            long coinsEarned = completedWords * 10 + completedSentences * 20;
            
            stats.put("date", date);
            stats.put("completedWords", completedWords);
            stats.put("completedSentences", completedSentences);
            stats.put("coinsEarned", coinsEarned);
            
        } catch (Exception e) {
            // 에러 발생 시 기본값 반환
            stats.put("date", date);
            stats.put("completedWords", 0);
            stats.put("completedSentences", 0);
            stats.put("coinsEarned", 0);
        }
        
        return stats;
    }

    /**
     * 노력왕 전체 랭킹 (일주일간 학습량 기준)
     */
    public List<RankingDto> getTopRankings() {
        List<User> users = userRepository.findAll();
        List<RankingDto> rankings = new ArrayList<>();
        
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        for (User user : users) {
            // 일주일간 학습한 단어 수
            long wordsLearned = userWordProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtAfter(
                    user.getId(), weekAgo);
            
            // 일주일간 학습한 문장 수
            long sentencesLearned = userSentenceProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtAfter(
                    user.getId(), weekAgo);
            
            // 디버깅 로그 추가
            System.out.println("랭킹 계산 - 사용자: " + user.getName() + 
                              ", 단어: " + wordsLearned + 
                              ", 문장: " + sentencesLearned + 
                              ", 일주일 전: " + weekAgo);
            
            // 총 학습량이 0보다 큰 경우만 랭킹에 포함
            if (wordsLearned > 0 || sentencesLearned > 0) {
                rankings.add(RankingDto.builder()
                    .rank(0) // 나중에 정렬 후 순위 부여
                    .name(user.getName())
                    .wordsLearned((int) wordsLearned)
                    .sentencesLearned((int) sentencesLearned)
                    .badge("일주일")
                    .build());
            }
        }
        
        // 총 학습량(단어+문장) 기준으로 정렬 및 순위 부여
        rankings.sort((a, b) -> (b.getWordsLearned() + b.getSentencesLearned()) - (a.getWordsLearned() + a.getSentencesLearned()));
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings.subList(0, Math.min(10, rankings.size())); // 상위 10명만
    }

    /**
     * 복습왕 랭킹 (learn_count 기준)
     */
    public List<RankingDto> getReviewRankings() {
        List<User> users = userRepository.findAll();
        List<RankingDto> rankings = new ArrayList<>();
        
        for (User user : users) {
            // 단어 복습 횟수 (learn_count 합계)
            long wordReviewCount = userWordProgressRepository.sumLearnCountByUserId(user.getId());
            
            // 문장 복습 횟수 (learn_count 합계)
            long sentenceReviewCount = userSentenceProgressRepository.sumLearnCountByUserId(user.getId());
            
            // 총 복습 횟수가 0보다 큰 경우만 랭킹에 포함
            if (wordReviewCount > 0 || sentenceReviewCount > 0) {
                rankings.add(RankingDto.builder()
                    .rank(0) // 나중에 정렬 후 순위 부여
                    .name(user.getName())
                    .wordsLearned((int) wordReviewCount)
                    .sentencesLearned((int) sentenceReviewCount)
                    .badge("복습")
                    .build());
            }
        }
        
        // 총 복습 횟수 기준으로 정렬 및 순위 부여
        rankings.sort((a, b) -> (b.getWordsLearned() + b.getSentencesLearned()) - (a.getWordsLearned() + a.getSentencesLearned()));
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings.subList(0, Math.min(10, rankings.size())); // 상위 10명만
    }

    /**
     * 특정 사용자의 노력왕 랭킹 순위 조회
     */
    public RankingDto getMyEffortRanking(Long userId) {
        List<User> users = userRepository.findAll();
        List<RankingDto> allRankings = new ArrayList<>();
        
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        for (User user : users) {
            // 일주일간 학습한 단어 수
            long wordsLearned = userWordProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtAfter(
                    user.getId(), weekAgo);
            
            // 일주일간 학습한 문장 수
            long sentencesLearned = userSentenceProgressRepository.countByUserIdAndIsLearnedTrueAndFirstLearnedAtAfter(
                    user.getId(), weekAgo);
            
            // 총 학습량이 0보다 큰 경우만 랭킹에 포함
            if (wordsLearned > 0 || sentencesLearned > 0) {
                allRankings.add(RankingDto.builder()
                    .rank(0) // 나중에 정렬 후 순위 부여
                    .name(user.getName())
                    .wordsLearned((int) wordsLearned)
                    .sentencesLearned((int) sentencesLearned)
                    .badge("일주일")
                    .build());
            }
        }
        
        // 총 학습량(단어+문장) 기준으로 정렬 및 순위 부여
        allRankings.sort((a, b) -> (b.getWordsLearned() + b.getSentencesLearned()) - (a.getWordsLearned() + a.getSentencesLearned()));
        for (int i = 0; i < allRankings.size(); i++) {
            allRankings.get(i).setRank(i + 1);
        }
        
        // 해당 사용자의 순위 찾기
        return allRankings.stream()
                .filter(ranking -> ranking.getName().equals(userRepository.findById(userId).map(User::getName).orElse("")))
                .findFirst()
                .orElse(null);
    }

    /**
     * 특정 사용자의 복습왕 랭킹 순위 조회
     */
    public RankingDto getMyReviewRanking(Long userId) {
        List<User> users = userRepository.findAll();
        List<RankingDto> allRankings = new ArrayList<>();
        
        for (User user : users) {
            // 단어 복습 횟수 합계
            long wordReviewCount = userWordProgressRepository.sumLearnCountByUserId(user.getId());
            
            // 문장 복습 횟수 합계
            long sentenceReviewCount = userSentenceProgressRepository.sumLearnCountByUserId(user.getId());
            
            // 총 복습 횟수가 0보다 큰 경우만 랭킹에 포함
            if (wordReviewCount > 0 || sentenceReviewCount > 0) {
                allRankings.add(RankingDto.builder()
                    .rank(0) // 나중에 정렬 후 순위 부여
                    .name(user.getName())
                    .wordsLearned((int) wordReviewCount)
                    .sentencesLearned((int) sentenceReviewCount)
                    .badge("복습")
                    .build());
            }
        }
        
        // 총 복습 횟수 기준으로 정렬 및 순위 부여
        allRankings.sort((a, b) -> (b.getWordsLearned() + b.getSentencesLearned()) - (a.getWordsLearned() + a.getSentencesLearned()));
        for (int i = 0; i < allRankings.size(); i++) {
            allRankings.get(i).setRank(i + 1);
        }
        
        // 해당 사용자의 순위 찾기
        return allRankings.stream()
                .filter(ranking -> ranking.getName().equals(userRepository.findById(userId).map(User::getName).orElse("")))
                .findFirst()
                .orElse(null);
    }

    private double calculateCompletionRate(long wordsLearned, long sentencesLearned) {
        long totalWords = wordRepository.countByIsActiveTrue();
        long totalSentences = sentenceRepository.countByIsActiveTrue();
        
        if (totalWords == 0 && totalSentences == 0) {
            return 0.0;
        }
        
        double wordCompletionRate = totalWords > 0 ? (double) wordsLearned / totalWords : 0;
        double sentenceCompletionRate = totalSentences > 0 ? (double) sentencesLearned / totalSentences : 0;
        
        return Math.round((wordCompletionRate + sentenceCompletionRate) / 2 * 100.0) / 100.0;
    }
} 