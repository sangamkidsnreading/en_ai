package com.example.kidsreading.service;

import com.example.kidsreading.dto.DashboardDto;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.entity.UserWordProgress;
import com.example.kidsreading.entity.UserSentenceProgress;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.WordRepository;
import com.example.kidsreading.repository.SentenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

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

    /**
     * 대시보드 메인 데이터 조회
     */
    public DashboardDto getDashboardData(Long userId) {
        try {
            // 실제 학습 데이터 조회
            List<UserWordProgress> wordProgressList = userWordProgressRepository.findByUserId(userId);
            List<UserSentenceProgress> sentenceProgressList = userSentenceProgressRepository.findByUserId(userId);
            User user = userRepository.findById(userId).orElse(null);
            
            // 전체 학습한 단어/문장 수 (isCompleted가 true인 것만)
            long totalWordsLearned = wordProgressList.stream()
                .filter(wp -> wp.getIsLearned() != null && wp.getIsLearned())
                .count();
            long totalSentencesLearned = sentenceProgressList.stream()
                .filter(sp -> sp.getIsCompleted() != null && sp.getIsCompleted())
                .count();
            
            // 오늘 학습한 단어/문장 수
            LocalDate today = LocalDate.now();
            long todayWords = wordProgressList.stream()
                .filter(wp -> wp.getUpdatedAt() != null && wp.getUpdatedAt().toLocalDate().equals(today))
                .count();
            long todaySentences = sentenceProgressList.stream()
                .filter(sp -> sp.getUpdatedAt() != null && sp.getUpdatedAt().toLocalDate().equals(today))
                .count();
            
            // 총 코인 계산 (CoinService에서 가져오거나 계산)
            int totalCoins = calculateTotalCoins(userId, totalWordsLearned, totalSentencesLearned);
            
            // 연속 학습일 계산
            int streakDays = calculateStreakDays(userId);
            
            // 레벨 계산 (100단어당 1레벨)
            int currentLevel = (int) (totalWordsLearned / 100) + 1;
            int levelProgress = (int) ((totalWordsLearned % 100) * 100 / 100);
            int wordsToNextLevel = 100 - (int) (totalWordsLearned % 100);
            
            // 이전 데이터와 비교하여 변화량 계산 (어제 대비)
            int previousWordsLearned = Math.max(0, (int) totalWordsLearned - (int) todayWords);
            int previousSentencesLearned = Math.max(0, (int) totalSentencesLearned - (int) todaySentences);
            int previousTotalCoins = Math.max(0, totalCoins - (int) (todayWords * 5 + todaySentences * 10));
            
            return DashboardDto.builder()
                    .wordsLearned((int) totalWordsLearned)
                    .sentencesLearned((int) totalSentencesLearned)
                    .totalCoins(totalCoins)
                    .streakDays(streakDays)
                    .currentLevel(currentLevel)
                    .levelProgress(levelProgress)
                    .wordsToNextLevel(wordsToNextLevel)
                    .completionRate(calculateCompletionRate(totalWordsLearned, totalSentencesLearned))
                    .totalWords((int) totalWordsLearned)
                    .totalSentences((int) totalSentencesLearned)
                    .dailyWordGoal(10)
                    .dailySentenceGoal(5)
                    .dailyWordProgress((int) todayWords)
                    .dailySentenceProgress((int) todaySentences)
                    .previousWordsLearned(previousWordsLearned)
                    .previousSentencesLearned(previousSentencesLearned)
                    .previousTotalCoins(previousTotalCoins)
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
     * 총 코인 계산
     */
    private int calculateTotalCoins(Long userId, long wordsLearned, long sentencesLearned) {
        try {
            // 학습한 단어당 5코인, 문장당 10코인
            int calculatedCoins = (int) (wordsLearned * 5 + sentencesLearned * 10);
            
            // 기본값 100코인 보장
            return Math.max(100, calculatedCoins);
        } catch (Exception e) {
            return 100;
        }
    }
    
    /**
     * 연속 학습일 계산
     */
    private int calculateStreakDays(Long userId) {
        try {
            // 간단한 연속 학습일 계산 (최근 7일 중 학습한 날 수)
            LocalDate today = LocalDate.now();
            int streakDays = 0;
            
            for (int i = 0; i < 7; i++) {
                LocalDate checkDate = today.minusDays(i);
                LocalDateTime startOfDay = checkDate.atStartOfDay();
                LocalDateTime endOfDay = checkDate.atTime(23, 59, 59);
                
                // 해당 날짜에 학습한 데이터가 있는지 확인
                List<UserWordProgress> allWordProgress = userWordProgressRepository.findByUserId(userId);
                List<UserSentenceProgress> allSentenceProgress = userSentenceProgressRepository.findByUserId(userId);
                
                boolean hasWordActivity = allWordProgress.stream()
                    .anyMatch(wp -> {
                        LocalDateTime updatedAt = wp.getUpdatedAt();
                        return updatedAt != null && 
                               updatedAt.isAfter(startOfDay) && 
                               updatedAt.isBefore(endOfDay);
                    });
                
                boolean hasSentenceActivity = allSentenceProgress.stream()
                    .anyMatch(sp -> {
                        LocalDateTime updatedAt = sp.getUpdatedAt();
                        return updatedAt != null && 
                               updatedAt.isAfter(startOfDay) && 
                               updatedAt.isBefore(endOfDay);
                    });
                
                if (hasWordActivity || hasSentenceActivity) {
                    streakDays++;
                } else {
                    break; // 연속이 끊어지면 중단
                }
            }
            
            return Math.max(1, streakDays);
        } catch (Exception e) {
            return 1;
        }
    }
    
    /**
     * 완료율 계산
     */
    private double calculateCompletionRate(long wordsLearned, long sentencesLearned) {
        // 전체 단어/문장 수 대비 학습 완료율
        long totalWords = wordRepository.count();
        long totalSentences = sentenceRepository.count();
        
        if (totalWords == 0 && totalSentences == 0) {
            return 0.0;
        }
        
        double wordRate = totalWords > 0 ? (double) wordsLearned / totalWords : 0.0;
        double sentenceRate = totalSentences > 0 ? (double) sentencesLearned / totalSentences : 0.0;
        
        return Math.round((wordRate + sentenceRate) / 2 * 100 * 10) / 10.0;
    }

    /**
     * 월간 달력 데이터 조회
     */
    public Map<String, Object> getCalendarData(Long userId, Integer year, Integer month) {
        try {
            Map<String, Object> calendarData = new HashMap<>();
            Map<String, Object> daysData = new HashMap<>();
            
            YearMonth yearMonth = YearMonth.of(year, month);
            int daysInMonth = yearMonth.lengthOfMonth();
            
            // 각 날짜별 데이터 생성 (실제로는 DB에서 조회해야 함)
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                String dateKey = date.toString();
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateKey);
                dayData.put("status", "not-started"); // 기본값
                dayData.put("wordsCompleted", 0);
                dayData.put("sentencesCompleted", 0);
                dayData.put("coinsEarned", 0);
                
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
            
            // 기본 뱃지 데이터 (실제로는 DB에서 조회해야 함)
            String[] badgeNames = {"첫 걸음", "열정 학습자", "단어 마스터", "골드 마스터", "전설 수집가"};
            String[] badgeIcons = {"🎯", "🔥", "📚", "🏆", "⭐"};
            boolean[] earned = {true, true, true, false, false};
            
            for (int i = 0; i < badgeNames.length; i++) {
                Map<String, Object> badge = new HashMap<>();
                badge.put("id", "badge_" + (i + 1));
                badge.put("name", badgeNames[i]);
                badge.put("icon", badgeIcons[i]);
                badge.put("description", badgeNames[i] + " 뱃지입니다.");
                badge.put("isEarned", earned[i]);
                badge.put("earnedDate", earned[i] ? LocalDate.now().minusDays(i) : null);
                badges.add(badge);
            }
            
            badgesData.put("badges", badges);
            badgesData.put("totalBadges", 5);
            badgesData.put("earnedBadges", 3);
            
            return badgesData;
        } catch (Exception e) {
            Map<String, Object> defaultBadges = new HashMap<>();
            defaultBadges.put("badges", new ArrayList<>());
            defaultBadges.put("totalBadges", 5);
            defaultBadges.put("earnedBadges", 3);
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
    public Map<String, Object> getLevelProgress(Long userId) {
        try {
            Map<String, Object> levelData = new HashMap<>();
            
            // 기본 레벨 데이터 (실제로는 DB에서 조회해야 함)
            levelData.put("currentLevel", 1);
            levelData.put("levelProgress", 0);
            levelData.put("wordsToNextLevel", 100);
            
            return levelData;
        } catch (Exception e) {
            Map<String, Object> defaultLevel = new HashMap<>();
            defaultLevel.put("currentLevel", 1);
            defaultLevel.put("levelProgress", 0);
            defaultLevel.put("wordsToNextLevel", 100);
            return defaultLevel;
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
            
            // 해당 날짜에 완료된 단어 수 조회 (기존 메서드 사용)
            List<UserWordProgress> wordProgressList = userWordProgressRepository.findByUserId(userId);
            long completedWords = wordProgressList.stream()
                .filter(wp -> wp.getIsLearned() != null && wp.getIsLearned() && 
                        wp.getCreatedAt() != null && 
                        wp.getCreatedAt().toLocalDate().equals(targetDate))
                .count();
            
            // 해당 날짜에 완료된 문장 수 조회 (기존 메서드 사용)
            List<UserSentenceProgress> sentenceProgressList = userSentenceProgressRepository.findByUserId(userId);
            long completedSentences = sentenceProgressList.stream()
                .filter(sp -> sp.getIsCompleted() != null && sp.getIsCompleted() && 
                        sp.getCreatedAt() != null && 
                        sp.getCreatedAt().toLocalDate().equals(targetDate))
                .count();
            
            // 해당 날짜에 획득한 코인 수 조회 (기본값 사용)
            long coinsEarned = 0; // TODO: 코인 히스토리 테이블이 있으면 구현
            
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
} 