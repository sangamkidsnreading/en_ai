package com.example.kidsreading.controller;

import com.example.kidsreading.dto.LevelProgressDto;
import com.example.kidsreading.service.CustomUserDetailsService;
import com.example.kidsreading.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/level")
@RequiredArgsConstructor
@Slf4j
public class LevelController {

    private final UserService userService;

    /**
     * 사용자의 레벨 진행도 조회
     */
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getLevelProgress(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).body(Map.of("error", "인증이 필요합니다."));
        }

        try {
            Long userId = user.getId();
            
            // UserService를 사용하여 레벨 설정을 고려한 진행도 계산
            LevelProgressDto levelProgress = userService.getLevelProgressForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("currentLevel", levelProgress.getCurrentLevel());
            response.put("levelProgress", levelProgress.getLevelProgress());
            response.put("wordsToNextLevel", levelProgress.getWordsToNextLevel());
            response.put("sentencesToNextLevel", levelProgress.getSentencesToNextLevel());
            
            log.info("레벨 진행도 조회 완료: userId={}, level={}, progress={}%", 
                    userId, levelProgress.getCurrentLevel(), levelProgress.getLevelProgress());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("레벨 진행도 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "레벨 진행도 조회에 실패했습니다."));
        }
    }

    /**
     * 레벨업 처리
     */
    @PostMapping("/levelup")
    public ResponseEntity<Map<String, Object>> levelUp(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).body(Map.of("error", "인증이 필요합니다."));
        }

        try {
            Long userId = user.getId();
            
            // 현재 레벨 진행도 확인
            LevelProgressDto currentProgress = userService.getLevelProgressForUser(userId);
            
            log.info("레벨업 요청: userId={}, currentLevel={}, progress={}%, wordsToNext={}, sentencesToNext={}", 
                    userId, currentProgress.getCurrentLevel(), currentProgress.getLevelProgress(),
                    currentProgress.getWordsToNextLevel(), currentProgress.getSentencesToNextLevel());
            
            // 레벨업 조건 확인 (100% 달성 시)
            if (currentProgress.getLevelProgress() >= 100) {
                // 실제 레벨업 로직 실행
                userService.levelUpUser(userId);
                
                // 레벨업 후 새로운 진행도 조회
                LevelProgressDto newProgress = userService.getLevelProgressForUser(userId);
                
                log.info("레벨업 성공: userId={}, oldLevel={}, newLevel={}", 
                        userId, currentProgress.getCurrentLevel(), newProgress.getCurrentLevel());
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "레벨업 성공!",
                    "oldLevel", currentProgress.getCurrentLevel(),
                    "newLevel", newProgress.getCurrentLevel(),
                    "newProgress", newProgress.getLevelProgress()
                ));
            } else {
                log.warn("레벨업 조건 불만족: userId={}, progress={}%, required=100%", 
                        userId, currentProgress.getLevelProgress());
                
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "레벨업 조건을 만족하지 않습니다.",
                    "currentProgress", currentProgress.getLevelProgress(),
                    "wordsToNextLevel", currentProgress.getWordsToNextLevel(),
                    "sentencesToNextLevel", currentProgress.getSentencesToNextLevel(),
                    "currentLevel", currentProgress.getCurrentLevel()
                ));
            }
            
        } catch (Exception e) {
            log.error("레벨업 처리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "레벨업 처리에 실패했습니다."));
        }
    }
}
