package com.example.kidsreading.controller;

import com.example.kidsreading.dto.DashboardDto;
import com.example.kidsreading.dto.TodayProgressDto;
import com.example.kidsreading.dto.RankingDto;
import com.example.kidsreading.dto.LevelProgressDto;
import com.example.kidsreading.service.DashboardService;
import com.example.kidsreading.service.MainContentService;
import com.example.kidsreading.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MainContentService mainContentService;

    /**
     * 대시보드 메인 데이터 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardDto> getDashboardStats(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            DashboardDto dashboardData = dashboardService.getDashboardData(user.getId());
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            // 에러 발생 시 기본 데이터 반환
            DashboardDto defaultData = DashboardDto.builder()
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
                    .build();
            return ResponseEntity.ok(defaultData);
        }
    }

    /**
     * 오늘의 학습 진행도 조회
     */
    @GetMapping("/today-progress")
    public ResponseEntity<TodayProgressDto> getTodayProgress(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            TodayProgressDto progress = mainContentService.getTodayProgress(user.getId());
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            // 에러 발생 시 기본 데이터 반환
            TodayProgressDto defaultProgress = TodayProgressDto.builder()
                    .words(new ArrayList<>())
                    .sentences(new ArrayList<>())
                    .coin(0)
                    .build();
            return ResponseEntity.ok(defaultProgress);
        }
    }

    /**
     * 월간 달력 데이터 조회
     */
    @GetMapping("/calendar/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getCalendarData(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            Map<String, Object> calendarData = dashboardService.getCalendarData(user.getId(), year, month);
            return ResponseEntity.ok(calendarData);
        } catch (Exception e) {
            Map<String, Object> defaultCalendar = new HashMap<>();
            defaultCalendar.put("calendarData", new HashMap<>());
            defaultCalendar.put("currentMonth", month);
            defaultCalendar.put("currentYear", year);
            return ResponseEntity.ok(defaultCalendar);
        }
    }

    /**
     * 뱃지 정보 조회
     */
    @GetMapping("/badges")
    public ResponseEntity<Map<String, Object>> getBadges(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            Map<String, Object> badgesData = dashboardService.getBadgesData(user.getId());
            return ResponseEntity.ok(badgesData);
        } catch (Exception e) {
            Map<String, Object> defaultBadges = new HashMap<>();
            defaultBadges.put("badges", new Object[]{});
            defaultBadges.put("totalBadges", 5);
            defaultBadges.put("earnedBadges", 3);
            return ResponseEntity.ok(defaultBadges);
        }
    }

    /**
     * 랭킹 정보 조회
     */
    @GetMapping("/rankings")
    public ResponseEntity<Map<String, Object>> getRankings(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            Map<String, Object> rankingsData = dashboardService.getRankingsData(user.getId());
            return ResponseEntity.ok(rankingsData);
        } catch (Exception e) {
            Map<String, Object> defaultRankings = new HashMap<>();
            defaultRankings.put("rankings", new Object[]{});
            defaultRankings.put("userRank", 0);
            return ResponseEntity.ok(defaultRankings);
        }
    }

    /**
     * 노력왕 전체 랭킹 리스트 조회
     */
    @GetMapping("/top-rankings")
    public ResponseEntity<List<RankingDto>> getTopRankings(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        List<RankingDto> rankings = dashboardService.getTopRankings();
        return ResponseEntity.ok(rankings);
    }

    /**
     * 레벨 진행도 조회
     */
    @GetMapping("/level-progress")
    public ResponseEntity<LevelProgressDto> getLevelProgress(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        LevelProgressDto progress = dashboardService.getLevelProgress(user.getId());
        return ResponseEntity.ok(progress);
    }

    /**
     * 일일 목표 진행도 조회
     */
    @GetMapping("/daily-goals")
    public ResponseEntity<Map<String, Object>> getDailyGoals(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            Map<String, Object> goalsData = dashboardService.getDailyGoals(user.getId());
            return ResponseEntity.ok(goalsData);
        } catch (Exception e) {
            Map<String, Object> defaultGoals = new HashMap<>();
            defaultGoals.put("dailyWordGoal", 10);
            defaultGoals.put("dailySentenceGoal", 5);
            defaultGoals.put("dailyWordProgress", 0);
            defaultGoals.put("dailySentenceProgress", 0);
            return ResponseEntity.ok(defaultGoals);
        }
    }

    /**
     * 특정 날짜의 학습 통계 조회 (캘린더 날짜 클릭 시)
     */
    @GetMapping("/stats/date")
    public ResponseEntity<Map<String, Object>> getStatsByDate(
            @RequestParam String date,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            Map<String, Object> dateStats = dashboardService.getStatsByDate(user.getId(), date);
            return ResponseEntity.ok(dateStats);
        } catch (Exception e) {
            Map<String, Object> defaultDateStats = new HashMap<>();
            defaultDateStats.put("date", date);
            defaultDateStats.put("completedWords", 0);
            defaultDateStats.put("completedSentences", 0);
            defaultDateStats.put("coinsEarned", 0);
            return ResponseEntity.ok(defaultDateStats);
        }
    }
}
