package com.example.kidsreading.controller;

import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.service.SentenceService;
import com.example.kidsreading.service.WordService;
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

        Long userId = 1L; // 임시 사용자 ID

        int completedWords = wordService.getCompletedWordsCount(userId, level, day);
        int totalWords = Math.max(wordService.getTotalWordsCount(level, day), 10); // 최소 10개

        int completedSentences = sentenceService.getCompletedSentencesCount(userId, level, day);
        int totalSentences = Math.max(sentenceService.getTotalSentencesCount(level, day), 5); // 최소 5개

        int coinsEarned = wordService.getCoinsEarned(userId, level, day);

        return ResponseEntity.ok(Map.of(
                "completedWords", completedWords,
                "totalWords", totalWords,
                "completedSentences", completedSentences,
                "totalSentences", totalSentences,
                "coinsEarned", coinsEarned
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
}