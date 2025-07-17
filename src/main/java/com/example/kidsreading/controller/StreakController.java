package com.example.kidsreading.controller;

import com.example.kidsreading.dto.UserStreakDto;
import com.example.kidsreading.service.CustomUserDetailsService;
import com.example.kidsreading.service.StreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/streak")
@RequiredArgsConstructor
@Slf4j
public class StreakController {

    private final StreakService streakService;

    /**
     * 사용자의 연속 학습일 정보 조회
     */
    @GetMapping
    public ResponseEntity<UserStreakDto> getUserStreak(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }

        try {
            UserStreakDto streakDto = streakService.getUserStreak(user.getId());
            return ResponseEntity.ok(streakDto);
        } catch (Exception e) {
            log.error("연속 학습일 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 연속 학습일 업데이트 (학습 완료 시 호출)
     */
    @PostMapping("/update")
    public ResponseEntity<UserStreakDto> updateStreak(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }

        try {
            UserStreakDto streakDto = streakService.updateStreak(user.getId());
            return ResponseEntity.ok(streakDto);
        } catch (Exception e) {
            log.error("연속 학습일 업데이트 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 연속 학습일 리셋
     */
    @PostMapping("/reset")
    public ResponseEntity<UserStreakDto> resetStreak(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }

        try {
            UserStreakDto streakDto = streakService.resetStreak(user.getId());
            return ResponseEntity.ok(streakDto);
        } catch (Exception e) {
            log.error("연속 학습일 리셋 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 연속 학습일 보너스 계산
     */
    @GetMapping("/bonus")
    public ResponseEntity<Map<String, Object>> getStreakBonus(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }

        try {
            int bonus = streakService.calculateStreakBonus(user.getId());
            return ResponseEntity.ok(Map.of("streakBonus", bonus));
        } catch (Exception e) {
            log.error("연속 학습일 보너스 계산 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 오늘 학습 여부 확인
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> isLearnedToday(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }

        try {
            boolean isLearnedToday = streakService.isLearnedToday(user.getId());
            return ResponseEntity.ok(Map.of("isLearnedToday", isLearnedToday));
        } catch (Exception e) {
            log.error("오늘 학습 여부 확인 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 기존 연속 학습일 데이터에 이메일 업데이트 (관리자용)
     */
    @PostMapping("/update-email")
    public ResponseEntity<Map<String, Object>> updateEmailForExistingStreaks() {
        try {
            streakService.updateEmailForExistingStreaks();
            return ResponseEntity.ok(Map.of("success", true, "message", "이메일 업데이트 완료"));
        } catch (Exception e) {
            log.error("이메일 업데이트 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 