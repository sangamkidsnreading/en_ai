package com.example.kidsreading.controller;

import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.dto.MainContentStatsDto;
import com.example.kidsreading.dto.TodayProgressDto;
import com.example.kidsreading.dto.UserCoinsDto;
import com.example.kidsreading.service.SentenceService;
import com.example.kidsreading.service.WordService;
import com.example.kidsreading.service.CoinService;
import com.example.kidsreading.service.MainContentService;
import com.example.kidsreading.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.service.UserService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RestController
@RequestMapping("/learning")
public class MainContentController {

    private static final Logger log = LoggerFactory.getLogger(MainContentController.class);

    private final WordService wordService;
    private final SentenceService sentenceService;
    private final CoinService coinService;
    private final MainContentService mainContentService;
    private final UserService userService;

    /**
     * 메인 학습 페이지 렌더링
     */
    @GetMapping("/main")
    public String mainPage(Model model, Authentication authentication) {
        model.addAttribute("currentLevel", 1);
        model.addAttribute("currentDay", 1);

        // 사용자 이름 추가
        String name = "학습자";
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            com.example.kidsreading.entity.User user = userService.findByEmail(email)
                .orElse(null);
            if (user != null && user.getName() != null) {
                name = user.getName();
            }
        }
        model.addAttribute("name", name);

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
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {

        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "인증이 필요합니다."));
        }

        Long wordId = Long.valueOf(request.get("wordId").toString());
        Boolean isCompleted = (Boolean) request.get("isCompleted");
        Boolean isFirstTime = (Boolean) request.get("isFirstTime");
        Long userId = user.getId();
        String email = user.getEmail();

        log.info("단어 진행도 업데이트: userId={}, wordId={}, isCompleted={}, isFirstTime={}", 
                userId, wordId, isCompleted, isFirstTime);

        wordService.updateWordProgress(userId, wordId, isCompleted, isFirstTime, email);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    /**
     * 문장 학습 진도 업데이트
     */
    @PostMapping("/api/progress/sentence")
    public ResponseEntity<Map<String, Object>> updateSentenceProgress(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {

        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "인증이 필요합니다."));
        }

        Long sentenceId = Long.valueOf(request.get("sentenceId").toString());
        Boolean isCompleted = (Boolean) request.get("isCompleted");
        Boolean isFirstTime = (Boolean) request.get("isFirstTime");
        Long userId = user.getId();
        String email = user.getEmail();

        log.info("문장 진행도 업데이트: userId={}, sentenceId={}, isCompleted={}, isFirstTime={}", 
                userId, sentenceId, isCompleted, isFirstTime);

        sentenceService.updateSentenceProgress(userId, sentenceId, isCompleted, isFirstTime, email);

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
            @RequestParam(defaultValue = "1") Integer day,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {

        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).body(Map.of(
                "completedWords", 0,
                "totalWords", 0,
                "completedSentences", 0,
                "totalSentences", 0,
                "coinsEarned", 0
            ));
        }

        Long userId = user.getId();

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
     * 레벨/Day별 학습 통계 조회 API (프론트엔드 통계 새로고침용)
     */
    @GetMapping("/api/stats")
    public ResponseEntity<Map<String, Integer>> getStats(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {

        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).body(Map.of(
                "completedWords", 0,
                "totalWords", 0,
                "completedSentences", 0,
                "totalSentences", 0,
                "coinsEarned", 0
            ));
        }

        Long userId = user.getId();

        int completedWords = wordService.getCompletedWordsCount(userId, level, day);
        int totalWords = wordService.getTotalWordsCount(level, day);
        int completedSentences = sentenceService.getCompletedSentencesCount(userId, level, day);
        int totalSentences = sentenceService.getTotalSentencesCount(level, day);
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


    @GetMapping("/api/progress/today")
    public ResponseEntity<TodayProgressDto> getTodayProgress(@AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        if (user == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mainContentService.getTodayProgress(user.getId()));
    }

    /**
     * 오늘의 단어/문장 개수 조회 API
     */
    @GetMapping("/api/today/counts")
    public ResponseEntity<Map<String, Object>> getTodayCounts(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(defaultValue = "1") Integer day,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        try {
            if (user == null) {
                log.warn("사용자 인증 정보가 없습니다.");
                return ResponseEntity.status(401).body(Map.of(
                    "todayWordsCount", 0,
                    "todaySentencesCount", 0,
                    "learnedWordsCount", 0,
                    "learnedSentencesCount", 0,
                    "totalLearnedCount", 0
                ));
            }
            
            Long userId = user.getId();
            
            // 오늘의 단어 개수
            int todayWordsCount = wordService.getWordsByLevelAndDay(level, day).size();
            
            // 오늘의 문장 개수
            int todaySentencesCount = sentenceService.getSentencesByLevelAndDay(level, day).size();
            
            // 오늘 학습한 단어 개수
            int learnedWordsCount = wordService.getCompletedWordsCount(userId, level, day);
            
            // 오늘 학습한 문장 개수
            int learnedSentencesCount = sentenceService.getCompletedSentencesCount(userId, level, day);
            
            Map<String, Object> result = Map.of(
                "todayWordsCount", todayWordsCount,
                "todaySentencesCount", todaySentencesCount,
                "learnedWordsCount", learnedWordsCount,
                "learnedSentencesCount", learnedSentencesCount,
                "totalLearnedCount", learnedWordsCount + learnedSentencesCount
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("오늘의 개수 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "todayWordsCount", 0,
                "todaySentencesCount", 0,
                "learnedWordsCount", 0,
                "learnedSentencesCount", 0,
                "totalLearnedCount", 0
            ));
        }
    }

    /**
     * 대시보드 통계 조회 API (어제와 비교)
     */
    @GetMapping("/api/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        try {
            if (user == null) {
                log.warn("사용자 인증 정보가 없습니다.");
                return ResponseEntity.status(401).body(Map.of(
                    "todayWordsLearned", 0,
                    "todaySentencesLearned", 0,
                    "todayCoinsEarned", 0,
                    "streakDays", 1,
                    "totalCoins", 0,
                    "wordsChangePercent", 0.0,
                    "sentencesChangePercent", 0.0,
                    "coinsChangePercent", 0.0
                ));
            }
            
            Long userId = user.getId();
            
            // 오늘 학습한 단어/문장 개수
            int todayWordsLearned = wordService.getTodayCompletedWordsCount(userId);
            int todaySentencesLearned = sentenceService.getTodayCompletedSentencesCount(userId);
            
            // 코인 정보는 /api/coins/user에서 가져오기
            UserCoinsDto userCoins = coinService.getCurrentUserCoins();
            int todayCoinsEarned = userCoins.getDailyCoins();
            int totalCoins = userCoins.getTotalCoins();
            int streakDays = coinService.getStreakDays(userId);
            
            // 어제 학습한 단어/문장 개수
            int yesterdayWordsLearned = wordService.getYesterdayCompletedWordsCount(userId);
            int yesterdaySentencesLearned = sentenceService.getYesterdayCompletedSentencesCount(userId);
            int yesterdayCoinsEarned = coinService.getYesterdayCoinsEarned(userId);
            
            // 변화율 계산
            double wordsChangePercent = calculateChangePercent(todayWordsLearned, yesterdayWordsLearned);
            double sentencesChangePercent = calculateChangePercent(todaySentencesLearned, yesterdaySentencesLearned);
            double coinsChangePercent = calculateChangePercent(todayCoinsEarned, yesterdayCoinsEarned);
            
            Map<String, Object> result = Map.of(
                "todayWordsLearned", todayWordsLearned,
                "todaySentencesLearned", todaySentencesLearned,
                "todayCoinsEarned", todayCoinsEarned,
                "streakDays", streakDays,
                "totalCoins", totalCoins,
                "wordsChangePercent", wordsChangePercent,
                "sentencesChangePercent", sentencesChangePercent,
                "coinsChangePercent", coinsChangePercent
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "todayWordsLearned", 0,
                "todaySentencesLearned", 0,
                "todayCoinsEarned", 0,
                "streakDays", 1,
                "totalCoins", 0,
                "wordsChangePercent", 0.0,
                "sentencesChangePercent", 0.0,
                "coinsChangePercent", 0.0
            ));
        }
    }

    /**
     * 학습량 그래프 데이터 조회 API
     */
    @GetMapping("/api/dashboard/graph")
    public ResponseEntity<Map<String, Object>> getLearningGraphData(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        
        try {
            if (user == null) {
                log.warn("사용자 인증 정보가 없습니다.");
                return ResponseEntity.status(401).body(Map.of(
                    "weeklyData", new ArrayList<>(),
                    "labels", new ArrayList<>(),
                    "wordsData", new ArrayList<>(),
                    "sentencesData", new ArrayList<>()
                ));
            }
            
            Long userId = user.getId();
            
            // 지난 7일간의 학습 데이터
            List<Map<String, Object>> weeklyData = new ArrayList<>();
            
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                int wordsCount = wordService.getCompletedWordsCountByDate(userId, date);
                int sentencesCount = sentenceService.getCompletedSentencesCountByDate(userId, date);
                
                Map<String, Object> dayData = Map.of(
                    "date", date.toString(),
                    "words", wordsCount,
                    "sentences", sentencesCount,
                    "total", wordsCount + sentencesCount
                );
                weeklyData.add(dayData);
            }
            
            Map<String, Object> result = Map.of(
                "weeklyData", weeklyData,
                "labels", weeklyData.stream().map(data -> data.get("date")).toList(),
                "wordsData", weeklyData.stream().map(data -> data.get("words")).toList(),
                "sentencesData", weeklyData.stream().map(data -> data.get("sentences")).toList()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("학습량 그래프 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "weeklyData", new ArrayList<>(),
                "labels", new ArrayList<>(),
                "wordsData", new ArrayList<>(),
                "sentencesData", new ArrayList<>()
            ));
        }
    }

    /**
     * 변화율 계산 헬퍼 메서드
     */
    private double calculateChangePercent(int today, int yesterday) {
        if (yesterday == 0) {
            return today > 0 ? 100.0 : 0.0;
        }
        return Math.round(((double) (today - yesterday) / yesterday) * 100.0 * 10.0) / 10.0;
    }

    @GetMapping("/student/kiriboca/components/main-content")
    public String mainContent(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
        log.info("user: {}", user);
        model.addAttribute("name", user.getName());
        // ... 필요시 다른 model 속성 추가 ...
        return "student/kiriboca/components/main-content";
    }

}