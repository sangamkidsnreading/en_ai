package com.example.kidsreading.config;

import com.example.kidsreading.entity.Sentence;
import com.example.kidsreading.entity.Word;
import com.example.kidsreading.repository.SentenceRepository;
import com.example.kidsreading.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // AdminInitializer 이후에 실행
public class DataInitializer implements CommandLineRunner {

    private final WordRepository wordRepository;
    private final SentenceRepository sentenceRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== DataInitializer 시작 ===");

        try {
            // 현재 단어 수 확인
            long currentWordCount = wordRepository.count();
            log.info("현재 데이터베이스의 단어 수: {}", currentWordCount);

            // 단어 데이터가 없으면 샘플 데이터 생성
            if (currentWordCount == 0) {
                createSampleWords();
                log.info("✅ 샘플 단어 데이터 생성 완료");
            } else {
                log.info("⚠️ 단어 데이터가 이미 존재합니다. 샘플 데이터 생성을 건너뜁니다.");
            }

            // 현재 문장 수 확인
            long currentSentenceCount = sentenceRepository.count();
            log.info("현재 데이터베이스의 문장 수: {}", currentSentenceCount);

            // 문장 데이터가 없으면 샘플 데이터 생성
            if (currentSentenceCount == 0) {
                createSampleSentences();
                log.info("✅ 샘플 문장 데이터 생성 완료");
            } else {
                log.info("⚠️ 문장 데이터가 이미 존재합니다. 샘플 데이터 생성을 건너뜁니다.");
            }

            // 최종 데이터 수 확인
            long finalWordCount = wordRepository.count();
            long finalSentenceCount = sentenceRepository.count();
            log.info("초기화 완료 후 - 단어: {}개, 문장: {}개", finalWordCount, finalSentenceCount);

        } catch (Exception e) {
            log.error("DataInitializer 실행 중 오류 발생", e);
            throw e;
        }

        log.info("=== DataInitializer 완료 ===");
    }

    private void createSampleWords() {
        String[][] wordData = {
            {"apple", "사과", "ˈæpəl", "/audio/words/11.mp3"},
            {"book", "책", "bʊk", "/audio/words/12.mp3"},
            {"cat", "고양이", "kæt", "/audio/words/13.mp3"},
            {"dog", "개", "dɔːɡ", "/audio/words/14.m4a"},
            {"egg", "달걀", "eɡ", "/audio/words/15.m4a"},
            {"fish", "물고기", "fɪʃ", "/audio/words/16.m4a"},
            {"green", "초록색", "ɡriːn", "/audio/words/17.m4a"},
            {"house", "집", "haʊs", "/audio/words/18.m4a"},
            {"ice", "얼음", "aɪs", "/audio/words/19.m4a"},
            {"jump", "점프하다", "dʒʌmp", "/audio/words/20.m4a"}
        };

        for (int i = 0; i < wordData.length; i++) {
            Word word = Word.builder()
                .text(wordData[i][0])
                .meaning(wordData[i][1])
                .pronunciation(wordData[i][2])
                .level(1)
                .day(1)
                .audioUrl(wordData[i][3])
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            wordRepository.save(word);
            log.info("단어 생성: {} - {}", wordData[i][0], wordData[i][1]);
        }
    }

    private void createSampleSentences() {
        String[][] sentenceData = {
            {"I like apples.", "나는 사과를 좋아합니다.", "/audio/sentences/14.mp3"},
            {"The cat is sleeping.", "고양이가 자고 있습니다.", "/audio/sentences/15.mp3"},
            {"She reads a book every day.", "그녀는 매일 책을 읽습니다.", "/audio/sentences/16.mp3"},
            {"We play in the park.", "우리는 공원에서 놉니다.", "/audio/sentences/17.mp3"},
            {"The sun is shining brightly.", "태양이 밝게 빛나고 있습니다.", "/audio/sentences/18.mp3"}
        };

        for (int i = 0; i < sentenceData.length; i++) {
            Sentence sentence = Sentence.builder()
                .englishText(sentenceData[i][0])
                .koreanTranslation(sentenceData[i][1])
                .level(1)
                .day(1)
                .audioUrl(sentenceData[i][2])
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            sentenceRepository.save(sentence);
            log.info("문장 생성: {} - {}", sentenceData[i][0], sentenceData[i][1]);
        }
    }
} 