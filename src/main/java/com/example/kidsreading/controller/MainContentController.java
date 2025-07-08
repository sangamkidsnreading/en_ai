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

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/learning")
public class MainContentController {

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

        // 레벨과 Day에 따른 다양한 단어 데이터
        String[][][] wordDataByLevel = {
            // Level 1
            {
                {"I", "나", "aɪ"}, {"am", "~이다", "æm"}, {"and", "그리고", "ænd"}, 
                {"are", "~이다", "ɑr"}, {"big", "큰", "bɪɡ"}, {"happy", "행복한", "ˈhæpi"}, 
                {"sad", "슬픈", "sæd"}, {"small", "작은", "smɔl"}, {"tall", "키가 큰", "tɔl"}, 
                {"we", "우리", "wi"}
            },
            // Level 2
            {
                {"apple", "사과", "ˈæpəl"}, {"book", "책", "bʊk"}, {"cat", "고양이", "kæt"}, 
                {"dog", "개", "dɔːɡ"}, {"egg", "달걀", "eɡ"}, {"fish", "물고기", "fɪʃ"}, 
                {"green", "초록색", "ɡriːn"}, {"house", "집", "haʊs"}, {"ice", "얼음", "aɪs"}, 
                {"jump", "점프하다", "dʒʌmp"}
            },
            // Level 3
            {
                {"water", "물", "ˈwɔːtər"}, {"school", "학교", "skuːl"}, {"friend", "친구", "frend"}, 
                {"mother", "어머니", "ˈmʌðər"}, {"father", "아버지", "ˈfɑːðər"}, {"sister", "자매", "ˈsɪstər"}, 
                {"brother", "형제", "ˈbrʌðər"}, {"teacher", "선생님", "ˈtiːtʃər"}, {"student", "학생", "ˈstuːdənt"}, 
                {"classroom", "교실", "ˈklæsruːm"}
            },
            // Level 4
            {
                {"computer", "컴퓨터", "kəmˈpjuːtər"}, {"telephone", "전화", "ˈteləfoʊn"}, {"television", "텔레비전", "ˈteləvɪʒən"}, 
                {"bicycle", "자전거", "ˈbaɪsɪkəl"}, {"airplane", "비행기", "ˈerpleɪn"}, {"mountain", "산", "ˈmaʊntən"}, 
                {"restaurant", "식당", "ˈrestərɑːnt"}, {"hospital", "병원", "ˈhɑːspɪtəl"}, {"library", "도서관", "ˈlaɪbreri"}, 
                {"supermarket", "슈퍼마켓", "ˈsuːpərmɑːrkət"}
            },
            // Level 5
            {
                {"environment", "환경", "ɪnˈvaɪrənmənt"}, {"education", "교육", "ˌedʒəˈkeɪʃən"}, {"technology", "기술", "tekˈnɑːlədʒi"}, 
                {"communication", "의사소통", "kəˌmjuːnəˈkeɪʃən"}, {"opportunity", "기회", "ˌɑːpərˈtuːnəti"}, {"responsibility", "책임", "rɪˌspɑːnsəˈbɪləti"}, 
                {"interesting", "흥미로운", "ˈɪntrəstɪŋ"}, {"important", "중요한", "ɪmˈpɔːrtənt"}, {"difficult", "어려운", "ˈdɪfɪkəlt"}, 
                {"beautiful", "아름다운", "ˈbjuːtɪfəl"}
            }
        };

        String[][] wordData = wordDataByLevel[level - 1];
        
        // Day에 따라 다른 단어 조합 생성
        int startIndex = (day - 1) * 5 % wordData.length;
        int wordsPerDay = Math.min(10, wordData.length);
        
        for (int i = 0; i < wordsPerDay; i++) {
            int index = (startIndex + i) % wordData.length;
            WordDto word = WordDto.builder()
                .id((long) (level * 1000 + day * 10 + i + 1))
                .text(wordData[index][0])
                .meaning(wordData[index][1])
                .pronunciation(wordData[index][2])
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

        // 레벨별 문장 데이터
        String[][][] sentenceDataByLevel = {
            // Level 1
            {
                {"I am happy.", "나는 행복합니다."},
                {"We are big.", "우리는 큽니다."},
                {"I am tall.", "나는 키가 큽니다."},
                {"We are small.", "우리는 작습니다."},
                {"I am sad.", "나는 슬픕니다."}
            },
            // Level 2
            {
                {"I like apples.", "나는 사과를 좋아합니다."},
                {"The cat is sleeping.", "고양이가 자고 있습니다."},
                {"My dog is big.", "내 개는 큽니다."},
                {"The fish is green.", "물고기가 초록색입니다."},
                {"I read a book.", "나는 책을 읽습니다."}
            },
            // Level 3
            {
                {"I go to school every day.", "나는 매일 학교에 갑니다."},
                {"My mother is a teacher.", "내 어머니는 선생님입니다."},
                {"I have a good friend.", "나는 좋은 친구가 있습니다."},
                {"The classroom is big.", "교실이 큽니다."},
                {"My father drinks water.", "내 아버지는 물을 마십니다."}
            },
            // Level 4
            {
                {"I use a computer every day.", "나는 매일 컴퓨터를 사용합니다."},
                {"We go to the restaurant.", "우리는 식당에 갑니다."},
                {"The airplane flies high.", "비행기가 높이 날아갑니다."},
                {"I read books in the library.", "나는 도서관에서 책을 읽습니다."},
                {"My bicycle is in the garage.", "내 자전거는 차고에 있습니다."}
            },
            // Level 5
            {
                {"Education is very important for everyone.", "교육은 모든 사람에게 매우 중요합니다."},
                {"Technology changes our environment.", "기술은 우리의 환경을 바꿉니다."},
                {"Communication is an important responsibility.", "의사소통은 중요한 책임입니다."},
                {"This opportunity is very interesting.", "이 기회는 매우 흥미롭습니다."},
                {"The beautiful mountain is difficult to climb.", "아름다운 산은 오르기 어렵습니다."}
            }
        };

        String[][] sentenceData = sentenceDataByLevel[level - 1];
        
        // Day에 따라 다른 문장 조합 생성
        int startIndex = (day - 1) * 2 % sentenceData.length;
        int sentencesPerDay = Math.min(5, sentenceData.length);
        
        for (int i = 0; i < sentencesPerDay; i++) {
            int index = (startIndex + i) % sentenceData.length;
            SentenceDto sentence = SentenceDto.builder()
                .id((long) (level * 1000 + day * 10 + i + 1))
                .english(sentenceData[index][0])
                .korean(sentenceData[index][1])
                .level(level)
                .day(day)
                .isActive(true)
                .build();
            sampleSentences.add(sentence);
        }

        return sampleSentences;
    }eping.",
            "She reads a book every day.",
            "We play in the park.",
            "The sun is shining brightly."
        };

        for (int i = 0; i < sentenceData.length; i++) {
            SentenceDto sentence = SentenceDto.builder()
                .id((long) (i + 1))
                .english(sentenceData[i])
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
    public ResponseEntity<Map<String, Object>> logAudioPlay(@RequestBody Map<String, Object> request) {
        // 음성 재생 로그 기록 로직
        return ResponseEntity.ok(Map.of("success", true));
    }
}