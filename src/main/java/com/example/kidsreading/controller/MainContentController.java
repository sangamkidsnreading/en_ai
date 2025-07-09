package com.example.kidsreading.controller;

import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.service.SentenceService;
import com.example.kidsreading.service.WordService;
import com.example.kidsreading.service.UserService;
import com.example.kidsreading.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/learning")
public class MainContentController {

    private static final Logger log = LoggerFactory.getLogger(MainContentController.class);

    private final WordService wordService;
    private final SentenceService sentenceService;
    private final UserService userService;
    private final CoinService coinService;

    /**
     * 메인 학습 페이지 렌더링
     */
    @GetMapping("/main")
    public String mainPage(Model model) {
        model.addAttribute("currentLevel", 1);
        model.addAttribute("currentDay", 1);
        return "learning/main";
    }

    /**
     * 단어 목록 조회 API
     */
    @GetMapping("/api/words")
    public ResponseEntity<List<WordDto>> getWords(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day) {

        List<WordDto> words = wordService.getWordsByLevelAndDay(level, day);
        return ResponseEntity.ok(words);
    }

    /**
     * 문장 목록 조회 API
     */
    @GetMapping("/api/sentences")
    public ResponseEntity<List<SentenceDto>> getSentences(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day) {

        List<SentenceDto> sentences = sentenceService.getSentencesByLevelAndDay(level, day);
        return ResponseEntity.ok(sentences);
    }

    // 샘플 단어 데이터 생성
    private List<WordDto> createSampleWords(Integer level, Integer day) {
        List<WordDto> sampleWords = new ArrayList<>();

        String[][] wordData = {
            {"apple", "사과", "ˈæpəl"},
            {"book", "책", "bʊk"},
            {"cat", "고양이", "kæt"},
            {"dog", "개", "dɔːɡ"},
            {"egg", "달걀", "eɡ"},
            {"fish", "물고기", "fɪʃ"},
            {"green", "초록색", "ɡriːn"},
            {"house", "집", "haʊs"},
            {"ice", "얼음", "aɪs"},
            {"jump", "점프하다", "dʒʌmp"}
        };

        for (int i = 0; i < wordData.length; i++) {
            WordDto word = WordDto.builder()
                .id((long) (i + 1))
                .text(wordData[i][0])
                .meaning(wordData[i][1])
                .pronunciation(wordData[i][2])
                .level(level)
                .day(day)
                .isActive(true)
                .build();
            sampleWords.add(word);
        }

        return sampleWords;
    }

    // 샘플 문장 데이터 생성
    private List<SentenceDto> createSampleSentences(Integer level, Integer day) {
        List<SentenceDto> sampleSentences = new ArrayList<>();

        String[] sentenceData = {
            "I like apples.",
            "The cat is sleeping.",
            "She reads a book every day.",
            "We play in the park.",
            "The sun is shining brightly."
        };

        for (int i = 0; i < sentenceData.length; i++) {
            SentenceDto sentence = SentenceDto.builder()
                .id((long) (i + 1))
                .text(sentenceData[i])
                .meaning(sentenceData[i] + " (한국어 의미)")
                .level(level)
                .day(day)
                .isActive(true)
                .build();
            sampleSentences.add(sentence);
        }

        return sampleSentences;
    }

    /**
     * 단어 학습 진도 업데이트
     */
    @PostMapping("/api/progress/word")
    public ResponseEntity<Map<String, Object>> updateWordProgress(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String username = authentication.getName();
        Long wordId = Long.valueOf(request.get("wordId").toString());
        Boolean isCompleted = (Boolean) request.get("isCompleted");

        log.info("단어 진행상황 업데이트 - 사용자: {}, 단어ID: {}, 완료: {}", username, wordId, isCompleted);

        try {
            Long userId = userService.getUserIdByUsername(username);
            wordService.updateWordProgress(userId, wordId, isCompleted);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "단어 진행상황이 업데이트되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("단어 진행상황 업데이트 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "진행상황 업데이트에 실패했습니다");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 문장 학습 진도 업데이트
     */
    @PostMapping("/api/progress/sentence")
    public ResponseEntity<Map<String, Object>> updateSentenceProgress(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String username = authentication.getName();
        Long sentenceId = Long.valueOf(request.get("sentenceId").toString());
        Boolean isCompleted = (Boolean) request.get("isCompleted");

        log.info("문장 진행상황 업데이트 - 사용자: {}, 문장ID: {}, 완료: {}", username, sentenceId, isCompleted);

        try {
            Long userId = userService.getUserIdByUsername(username);
            sentenceService.updateSentenceProgress(userId, sentenceId, isCompleted);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "문장 진행상황이 업데이트되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("문장 진행상황 업데이트 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "진행상황 업데이트에 실패했습니다");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 오늘의 학습 통계 조회
     */
    @GetMapping("/api/stats/today")
    public ResponseEntity<Map<String, Object>> getTodayStats(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("사용자 {} - Level {} Day {} 통계 조회", username, level, day);

        Map<String, Object> stats = new HashMap<>();

        try {
            Long userId = userService.getUserIdByUsername(username);

            // 총 단어/문장 수
            long totalWords = wordService.getTotalWordsCount(level, day);
            long totalSentences = sentenceService.getTotalSentencesCount(level, day);

            // 완료된 단어/문장 수 (사용자별)
            long completedWords = wordService.getCompletedWordsCount(userId, level, day);
            long completedSentences = sentenceService.getCompletedSentencesCount(userId, level, day);

            stats.put("totalWords", totalWords);
            stats.put("totalSentences", totalSentences);
            stats.put("completedWords", completedWords);
            stats.put("completedSentences", completedSentences);
            stats.put("coinsEarned", 0); // 추후 구현

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("통계 조회 실패", e);
            stats.put("error", "통계 조회에 실패했습니다");
            return ResponseEntity.internalServerError().body(stats);
        }
    }

    @GetMapping("/api/stats/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeStats(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("실시간 통계 조회 - 사용자: {}, Level: {}, Day: {}", username, level, day);

        Map<String, Object> stats = new HashMap<>();

        try {
            Long userId = userService.getUserIdByUsername(username);

            // 실시간 진행률 조회
            long totalWords = wordService.getTotalWordsCount(level, day);
            long totalSentences = sentenceService.getTotalSentencesCount(level, day);
            long completedWords = wordService.getCompletedWordsCount(userId, level, day);
            long completedSentences = sentenceService.getCompletedSentencesCount(userId, level, day);
            long studiedWords = wordService.getStudiedWordsCount(userId, level, day);
            long studiedSentences = sentenceService.getStudiedSentencesCount(userId, level, day);

            // 진행률 계산
            double wordProgress = totalWords > 0 ? (double) completedWords / totalWords * 100 : 0;
            double sentenceProgress = totalSentences > 0 ? (double) completedSentences / totalSentences * 100 : 0;

            stats.put("totalWords", totalWords);
            stats.put("totalSentences", totalSentences);
            stats.put("completedWords", completedWords);
            stats.put("completedSentences", completedSentences);
            stats.put("studiedWords", studiedWords);
            stats.put("studiedSentences", studiedSentences);
            stats.put("wordProgress", Math.round(wordProgress * 10) / 10.0);
            stats.put("sentenceProgress", Math.round(sentenceProgress * 10) / 10.0);
            stats.put("level", level);
            stats.put("day", day);

            log.info("실시간 통계 조회 완료 - 단어: {}/{}, 문장: {}/{}", completedWords, totalWords, completedSentences, totalSentences);

        } catch (Exception e) {
            log.error("실시간 통계 조회 실패", e);
            stats.put("error", "통계 조회에 실패했습니다");
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * 단어 즐겨찾기 토글
     */
    @PostMapping("/api/words/{wordId}/favorite")
    public ResponseEntity<Map<String, Object>> toggleWordFavorite(@PathVariable Long wordId) {
        Long userId = 1L; // 임시 사용자 ID

        boolean isFavorite = wordService.toggleWordFavorite(userId, wordId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "isFavorite", isFavorite
        ));
    }

    /**
     * 음성 재생 로그 기록
     */
    @PostMapping("/api/audio/play")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logAudioPlay(@RequestBody Map<String, Object> request) {
        // 응답 즉시 반환 (로깅은 비동기 처리)
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        // 로깅은 별도 스레드에서 처리
        CompletableFuture.runAsync(() -> {
            try {
                String type = (String) request.get("type");
                Long itemId = Long.valueOf(request.get("itemId").toString());
                log.info("Audio play logged: {} - {}", type, itemId);
            } catch (Exception e) {
                log.error("Error logging audio play", e);
            }
        });

        return ResponseEntity.ok(response);
    }

    /**
     * learning_settings 기반 단어 코인 지급
     */
    @PostMapping("/api/coins/word")
    public ResponseEntity<Map<String, Object>> addWordCoins() {
        try {
            var coinSettings = coinService.getCoinSettings();
            int wordCoins = coinSettings.getWordCoin() != null ? coinSettings.getWordCoin() : 1;

            var result = coinService.addCurrentUserWordCoins();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("wordCoins", wordCoins);
            response.put("coinResult", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("단어 코인 지급 실패", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * learning_settings 기반 문장 코인 지급
     */
    @PostMapping("/api/coins/sentence")
    public ResponseEntity<Map<String, Object>> addSentenceCoins() {
        try {
            var coinSettings = coinService.getCoinSettings();
            int sentenceCoins = coinSettings.getSentenceCoin() != null ? coinSettings.getSentenceCoin() : 3;

            var result = coinService.addCurrentUserSentenceCoins();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sentenceCoins", sentenceCoins);
            response.put("coinResult", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("문장 코인 지급 실패", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * 연속 학습 보너스 코인 지급
     */
    @PostMapping("/api/coins/streak")
    public ResponseEntity<Map<String, Object>> addStreakBonus() {
        try {
            var coinSettings = coinService.getCoinSettings();
            int streakBonus = coinSettings.getStreakBonus() != null ? coinSettings.getStreakBonus() : 5;

            // 현재는 간단하게 보너스만 지급 (실제로는 연속 학습 조건 체크 필요)
            var result = coinService.addCurrentUserWordCoins(); // 임시로 단어 코인 메서드 사용

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("streakBonus", streakBonus);
            response.put("coinResult", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("보너스 코인 지급 실패", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}