
package com.example.kidsreading.controller;

import com.example.kidsreading.dto.LearningSettingsDto;
import com.example.kidsreading.dto.UserCoinsDto;
import com.example.kidsreading.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
@Slf4j
public class CoinController {

    private final CoinService coinService;

    @GetMapping("/user")
    public ResponseEntity<UserCoinsDto> getCurrentUserCoins() {
        try {
            UserCoinsDto coins = coinService.getCurrentUserCoins();
            return ResponseEntity.ok(coins);
        } catch (Exception e) {
            log.error("코인 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/word")
    public ResponseEntity<UserCoinsDto> addWordCoins() {
        try {
            UserCoinsDto coins = coinService.addCurrentUserWordCoins();
            return ResponseEntity.ok(coins);
        } catch (Exception e) {
            log.error("단어 코인 추가 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sentence")
    public ResponseEntity<UserCoinsDto> addSentenceCoins() {
        try {
            UserCoinsDto coins = coinService.addCurrentUserSentenceCoins();
            return ResponseEntity.ok(coins);
        } catch (Exception e) {
            log.error("문장 코인 추가 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/streak")
    public ResponseEntity<UserCoinsDto> addStreakBonus() {
        try {
            UserCoinsDto coins = coinService.addCurrentUserStreakBonus();
            return ResponseEntity.ok(coins);
        } catch (Exception e) {
            log.error("연속 학습 보너스 추가 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<UserCoinsDto>> getCoinHistory(@RequestParam(defaultValue = "7") int days) {
        try {
            String userId = coinService.getCurrentUserId();
            List<UserCoinsDto> history = coinService.getUserCoinHistory(userId, days);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("코인 히스토리 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<LearningSettingsDto> getCoinSettings() {
        try {
            LearningSettingsDto settings = coinService.getCoinSettings();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("코인 설정 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<Integer> getTotalCoins(@PathVariable String userId) {
        try {
            Integer totalCoins = coinService.getTotalCoins(userId);
            return ResponseEntity.ok(totalCoins);
        } catch (Exception e) {
            log.error("총 코인 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
