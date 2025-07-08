package com.example.kidsreading.controller;

import com.example.kidsreading.dto.UserCoinsDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.UserCoinDto;
import com.example.kidsreading.service.WordService;
import com.example.kidsreading.service.SentenceService;
import com.example.kidsreading.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.example.kidsreading.entity.Word;
import com.example.kidsreading.entity.Sentence;

@RestController
@RequestMapping("/api/sidebar")
@RequiredArgsConstructor
@Slf4j
public class SidebarController {

    private final WordService wordService;
    private final SentenceService sentenceService;
    private final CoinService coinService;

    private String getCurrentUserId(Authentication authentication) {
        // 실제 사용자 ID를 가져오는 로직 구현 필요
        // 예시: authentication.getName() 또는 authentication.getPrincipal()을 사용하여 사용자 ID 추출
        // 현재는 임시로 "1" 반환
        return "1";
    }

    /**
     * 레벨과 Day에 따른 카드 필터링
     */
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> filterCards(
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer day,
            Authentication authentication) {

        try {
            if (level == null || level <= 0 || level > 10) level = 1;
            if (day == null || day <= 0 || day > 5) day = 1;

            System.out.println("🔍 카드 필터링 요청 - Level: " + level + ", Day: " + day);

            // 단어와 문장 조회 (이미 DTO로 반환됨)
            List<WordDto> wordDtos = wordService.getWordsByLevelAndDay(level, day);
            List<SentenceDto> sentenceDtos = sentenceService.getSentencesByLevelAndDay(level, day);

            System.out.println("📊 조회 결과 - 단어: " + wordDtos.size() + "개, 문장: " + sentenceDtos.size() + "개");

            // 사용자 코인 정보 조회
            String userId = getCurrentUserId(authentication);
            UserCoinsDto userCoins = coinService.getUserCoins(userId);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("words", wordDtos);
            response.put("sentences", sentenceDtos);

            // 통계 정보 추가
            Map<String, Object> stats = new HashMap<>();
            stats.put("completedWords", 0);
            stats.put("totalWords", 10);
            stats.put("completedSentences", 0);
            stats.put("totalSentences", 5);
            stats.put("coinsEarned", userCoins.getDailyCoins()); // 실제 코인 데이터
            stats.put("totalCoins", userCoins.getTotalCoins()); // 총 코인
            response.put("stats", stats);
            response.put("coins", userCoins); // 코인 정보 추가
            response.put("totalWords", wordDtos.size());
            response.put("totalSentences", sentenceDtos.size());
            response.put("level", level);
            response.put("day", day);
            response.put("success", true);

            System.out.println("✅ 필터링 완료 - 단어: " + wordDtos.size() + "개, 문장: " + sentenceDtos.size() + "개");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ 카드 필터링 실패: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "카드 필터링에 실패했습니다: " + e.getMessage());
            errorResponse.put("words", new ArrayList<>());
            errorResponse.put("sentences", new ArrayList<>());
            errorResponse.put("totalWords", 0);
            errorResponse.put("totalSentences", 0);
            errorResponse.put("level", level != null ? level : 1);
            errorResponse.put("day", day != null ? day : 1);
            errorResponse.put("success", false);

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    /**
     * 검색 기능
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCards(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day) {

        log.info("🔍 카드 검색 요청 - Query: '{}', Level: {}, Day: {}", query, level, day);

        try {
            // 단어 검색 (영어, 한국어 모두)
            List<WordDto> allWords = wordService.getWordsByLevelAndDay(level, day);
            List<WordDto> filteredWords = allWords.stream()
                .filter(word -> 
                    word.getText().toLowerCase().contains(query.toLowerCase()) ||
                    word.getMeaning().toLowerCase().contains(query.toLowerCase()))
                .toList();

            // 문장 검색 (영어, 한국어 모두)
            List<SentenceDto> allSentences = sentenceService.getSentencesByLevelAndDay(level, day);
            List<SentenceDto> filteredSentences = allSentences.stream()
                .filter(sentence -> 
                    sentence.getEnglish().toLowerCase().contains(query.toLowerCase()) ||
                    (sentence.getKorean() != null && sentence.getKorean().toLowerCase().contains(query.toLowerCase())))
                .toList();

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("words", filteredWords);
            response.put("sentences", filteredSentences);
            response.put("query", query);
            response.put("level", level);
            response.put("day", day);
            response.put("totalWords", filteredWords.size());
            response.put("totalSentences", filteredSentences.size());

            log.info("✅ 검색 완료 - 단어: {}개, 문장: {}개", filteredWords.size(), filteredSentences.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 카드 검색 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용 가능한 레벨 목록 조회
     */
    @GetMapping("/levels")
    public ResponseEntity<List<Integer>> getAvailableLevels() {
        try {
            List<Integer> levels = wordService.getAvailableLevels();
            return ResponseEntity.ok(levels);
        } catch (Exception e) {
            log.error("❌ 레벨 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 레벨의 사용 가능한 Day 목록 조회
     */
    @GetMapping("/days")
    public ResponseEntity<List<Integer>> getAvailableDays(@RequestParam Integer level) {
        try {
            List<Integer> days = wordService.getAvailableDaysByLevel(level);
            return ResponseEntity.ok(days);
        } catch (Exception e) {
            log.error("❌ Day 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}