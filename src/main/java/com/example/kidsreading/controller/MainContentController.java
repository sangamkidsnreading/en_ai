package com.example.kidsreading.controller;

import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.service.SentenceService;
import com.example.kidsreading.service.WordService;
import com.example.kidsreading.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            @RequestBody Map<String, Object> request) {

        Long wordId = Long.valueOf(request.get("wordId").toString());
        Boolean isCompleted = (Boolean) request.get("isCompleted");

        // 사용자 ID는 세션이나 인증에서 가져와야 함 (여기서는 임시로 1L 사용)
        Long userId = 1L;

        wordService.updateWordProgress(userId, wordId, isCompleted);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "coinsEarned", 1
        ));
    }

    /**
     * 문장 학습 진도 업데이트
     */
    @PostMapping("/api/progress/sentence")
    public ResponseEntity<Map<String, Object>> updateSentenceProgress(
            @RequestBody Map<String, Object> request) {

        Long sentenceId = Long.valueOf(request.get("sentenceId").toString());
        Boolean isCompleted = (Boolean) request.get("isCompleted");

        // 사용자 ID는 세션이나 인증에서 가져와야 함 (여기서는 임시로 1L 사용)
        Long userId = 1L;

        sentenceService.updateSentenceProgress(userId, sentenceId, isCompleted);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "coinsEarned", 3
        ));
    }

    /**
     * 오늘의 학습 통계 조회
     */
    @GetMapping("/api/stats/today")
    public ResponseEntity<Map<String, Integer>> getTodayStats(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day) {

        // 현재 사용자 ID 가져오기 (실제로는 인증에서 가져와야 함)
        String currentUserId = coinService.getCurrentUserId();
        
        int completedWords = wordService.getCompletedWordsCountByUserId(currentUserId, level, day);
        int totalWords = wordService.getTotalWordsCount(level, day);

        int completedSentences = sentenceService.getCompletedSentencesCountByUserId(currentUserId, level, day);
        int totalSentences = sentenceService.getTotalSentencesCount(level, day);

        int coinsEarned = wordService.getCoinsEarnedByUserId(currentUserId, level, day);

        return ResponseEntity.ok(Map.of(
                "completedWords", completedWords,
                "totalWords", totalWords,
                "completedSentences", completedSentences,
                "totalSentences", totalSentences,
                "coinsEarned", coinsEarned
        ));
    }

    /**
     * 실시간 학습 진행도 조회 API
     */
    @GetMapping("/api/progress/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeProgress(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day) {

        String currentUserId = coinService.getCurrentUserId();
        
        int completedWords = wordService.getCompletedWordsCountByUserId(currentUserId, level, day);
        int totalWords = wordService.getTotalWordsCount(level, day);

        int completedSentences = sentenceService.getCompletedSentencesCountByUserId(currentUserId, level, day);
        int totalSentences = sentenceService.getTotalSentencesCount(level, day);

        // 진행률 계산
        double wordProgress = totalWords > 0 ? (double) completedWords / totalWords * 100 : 0;
        double sentenceProgress = totalSentences > 0 ? (double) completedSentences / totalSentences * 100 : 0;

        return ResponseEntity.ok(Map.of(
                "completedWords", completedWords,
                "totalWords", totalWords,
                "completedSentences", completedSentences,
                "totalSentences", totalSentences,
                "wordProgress", Math.round(wordProgress * 10) / 10.0,
                "sentenceProgress", Math.round(sentenceProgress * 10) / 10.0,
                "wordProgressText", completedWords + "/" + totalWords,
                "sentenceProgressText", completedSentences + "/" + totalSentences
        ));
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