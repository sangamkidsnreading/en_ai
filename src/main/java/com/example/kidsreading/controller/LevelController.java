package com.example.kidsreading.controller;

import com.example.kidsreading.service.CustomUserDetailsService;
import com.example.kidsreading.service.WordService;
import com.example.kidsreading.service.SentenceService;
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

    private final WordService wordService;
    private final SentenceService sentenceService;

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
            
            // 현재 레벨 (임시로 1로 설정)
            int currentLevel = 1;
            
            // 완료된 단어 수 (전체)
            int completedWords = wordService.getCompletedWordsCount(userId, 0, 0);
            
            // 완료된 문장 수 (전체)
            int completedSentences = sentenceService.getCompletedSentencesCount(userId, 0, 0);
            
            // 전체 단어 수
            int totalWords = wordService.getTotalWordsCount(0, 0);
            
            // 전체 문장 수
            int totalSentences = sentenceService.getTotalSentencesCount(0, 0);
            
            // 레벨 진행도 계산 (단어와 문장의 완료율을 기반으로)
            double levelProgress = 0.0;
            if (totalWords > 0 || totalSentences > 0) {
                double wordProgress = totalWords > 0 ? (double) completedWords / totalWords * 50 : 0;
                double sentenceProgress = totalSentences > 0 ? (double) completedSentences / totalSentences * 50 : 0;
                levelProgress = wordProgress + sentenceProgress;
            }
            
            // 다음 레벨까지 필요한 단어/문장 수 (임시 계산)
            int wordsToNextLevel = Math.max(0, 100 - completedWords);
            int sentencesToNextLevel = Math.max(0, 50 - completedSentences);
            
            Map<String, Object> response = new HashMap<>();
            response.put("currentLevel", currentLevel);
            response.put("levelProgress", Math.round(levelProgress * 100.0) / 100.0);
            response.put("completedWords", completedWords);
            response.put("totalWords", totalWords);
            response.put("completedSentences", completedSentences);
            response.put("totalSentences", totalSentences);
            response.put("wordsToNextLevel", wordsToNextLevel);
            response.put("sentencesToNextLevel", sentencesToNextLevel);
            
            log.info("레벨 진행도 조회 완료: userId={}, level={}, progress={}%", 
                    userId, currentLevel, levelProgress);
            
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
            int completedWords = wordService.getCompletedWordsCount(userId, 0, 0);
            int completedSentences = sentenceService.getCompletedSentencesCount(userId, 0, 0);
            int totalWords = wordService.getTotalWordsCount(0, 0);
            int totalSentences = sentenceService.getTotalSentencesCount(0, 0);
            
            double levelProgress = 0.0;
            if (totalWords > 0 || totalSentences > 0) {
                double wordProgress = totalWords > 0 ? (double) completedWords / totalWords * 50 : 0;
                double sentenceProgress = totalSentences > 0 ? (double) completedSentences / totalSentences * 50 : 0;
                levelProgress = wordProgress + sentenceProgress;
            }
            
            // 레벨업 조건 확인 (100% 달성 시)
            if (levelProgress >= 100.0) {
                // 여기에 실제 레벨업 로직 추가 (UserLevels 엔티티 사용)
                log.info("레벨업 성공: userId={}", userId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "레벨업 성공!",
                    "newLevel", 2 // 임시로 2레벨로 설정
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "레벨업 조건을 만족하지 않습니다.",
                    "currentProgress", levelProgress
                ));
            }
            
        } catch (Exception e) {
            log.error("레벨업 처리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "레벨업 처리에 실패했습니다."));
        }
    }
}
