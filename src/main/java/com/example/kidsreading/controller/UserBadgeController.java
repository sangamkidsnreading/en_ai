package com.example.kidsreading.controller;

import com.example.kidsreading.dto.UserBadgeDto;
import com.example.kidsreading.service.UserBadgeService;
import com.example.kidsreading.service.BadgeEarningService;
import com.example.kidsreading.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-badges")
@RequiredArgsConstructor
public class UserBadgeController {

    private final UserBadgeService userBadgeService;
    private final BadgeEarningService badgeEarningService;

    /**
     * 현재 사용자의 모든 뱃지 조회
     */
    @GetMapping("/my-badges")
    public ResponseEntity<List<UserBadgeDto>> getMyBadges() {
        Long userId = getCurrentUserId();
        List<UserBadgeDto> badges = userBadgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }

    /**
     * 현재 사용자의 표시 가능한 뱃지 조회
     */
    @GetMapping("/my-displayed-badges")
    public ResponseEntity<List<UserBadgeDto>> getMyDisplayedBadges() {
        Long userId = getCurrentUserId();
        List<UserBadgeDto> badges = userBadgeService.getDisplayedUserBadges(userId);
        return ResponseEntity.ok(badges);
    }

    /**
     * 현재 사용자의 최근 획득 뱃지 조회
     */
    @GetMapping("/my-recent-badges")
    public ResponseEntity<List<UserBadgeDto>> getMyRecentBadges() {
        Long userId = getCurrentUserId();
        List<UserBadgeDto> badges = userBadgeService.getRecentUserBadges(userId);
        return ResponseEntity.ok(badges);
    }

    /**
     * 뱃지 획득
     */
    @PostMapping("/earn/{badgeId}")
    public ResponseEntity<UserBadgeDto> earnBadge(@PathVariable Long badgeId) {
        Long userId = getCurrentUserId();
        UserBadgeDto earnedBadge = userBadgeService.earnBadge(userId, badgeId);
        return ResponseEntity.ok(earnedBadge);
    }

    /**
     * 뱃지 표시/숨김 토글
     */
    @PutMapping("/{userBadgeId}/toggle-display")
    public ResponseEntity<UserBadgeDto> toggleBadgeDisplay(@PathVariable Long userBadgeId) {
        UserBadgeDto updatedBadge = userBadgeService.toggleBadgeDisplay(userBadgeId);
        return ResponseEntity.ok(updatedBadge);
    }

    /**
     * 뱃지 삭제
     */
    @DeleteMapping("/{userBadgeId}")
    public ResponseEntity<Map<String, String>> deleteUserBadge(@PathVariable Long userBadgeId) {
        userBadgeService.deleteUserBadge(userBadgeId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "뱃지가 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 사용자의 뱃지 통계
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyBadgeStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBadges", userBadgeService.getUserBadgeCount(userId));
        stats.put("displayedBadges", userBadgeService.getDisplayedUserBadgeCount(userId));
        stats.put("recentBadges", userBadgeService.getRecentUserBadges(userId));
        // 상세 진행도 추가
        BadgeEarningService.UserBadgeStatsDto progress = badgeEarningService.getUserBadgeStats(userId);
        stats.put("attendanceCount", progress.getAttendanceCount());
        stats.put("streakCount", progress.getStreakCount());
        stats.put("completedWordsCount", progress.getCompletedWordsCount());
        stats.put("completedSentencesCount", progress.getCompletedSentencesCount());
        stats.put("wordReviewCount", progress.getWordReviewCount());
        stats.put("sentenceReviewCount", progress.getSentenceReviewCount());
        return ResponseEntity.ok(stats);
    }

    /**
     * 특정 뱃지 획득 여부 확인
     */
    @GetMapping("/check/{badgeId}")
    public ResponseEntity<Map<String, Boolean>> checkBadgeEarned(@PathVariable Long badgeId) {
        Long userId = getCurrentUserId();
        boolean hasEarned = userBadgeService.hasUserEarnedBadge(userId, badgeId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasEarned", hasEarned);
        return ResponseEntity.ok(response);
    }

    /**
     * 뱃지 획득 테스트 (개발용)
     */
    @PostMapping("/test-earn/{badgeId}")
    public ResponseEntity<Map<String, Object>> testEarnBadge(@PathVariable Long badgeId) {
        try {
            Long userId = getCurrentUserId();
            UserBadgeDto earnedBadge = userBadgeService.earnBadge(userId, badgeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "뱃지 획득 성공!");
            response.put("badge", earnedBadge);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "뱃지 획득 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 뱃지 통계 테스트 (개발용)
     */
    @GetMapping("/test-stats")
    public ResponseEntity<Map<String, Object>> testBadgeStats() {
        try {
            Long userId = getCurrentUserId();
            
            // BadgeEarningService의 통계 조회
            BadgeEarningService.UserBadgeStatsDto stats = badgeEarningService.getUserBadgeStats(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "통계 조회 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 뱃지 자동 획득 테스트 (개발용)
     */
    @PostMapping("/test-auto-earn")
    public ResponseEntity<Map<String, Object>> testAutoEarnBadges() {
        try {
            Long userId = getCurrentUserId();
            
            // 뱃지 자동 획득 체크 실행
            badgeEarningService.checkBadgesByUserStats(userId);
            
            // 획득된 뱃지 목록 조회
            List<UserBadgeDto> userBadges = userBadgeService.getUserBadges(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "뱃지 자동 획득 체크 완료");
            response.put("totalBadges", userBadges.size());
            response.put("badges", userBadges);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "뱃지 자동 획득 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 대시보드 새로고침(뱃지 리로드) API
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshUserBadges() {
        Long userId = getCurrentUserId();
        String email = getCurrentUserEmail();
        badgeEarningService.checkBadgesByUserStats(userId);
        List<UserBadgeDto> badges = userBadgeService.getUserBadges(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("email", email);
        response.put("badges", badges);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자 ID 가져오기
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        }
        throw new RuntimeException("사용자 인증 정보를 찾을 수 없습니다.");
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                    (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return userPrincipal.getUser().getEmail();
        }
        return null;
    }
} 