package com.example.kidsreading.service;

import com.example.kidsreading.dto.MainContentStatsDto;
import com.example.kidsreading.dto.TodayProgressDto;
import com.example.kidsreading.entity.LevelSettings;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.LevelSettingsRepository;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainContentService {
    @Autowired
    private UserWordProgressRepository wordRepo;
    @Autowired
    private UserSentenceProgressRepository sentenceRepo;
    @Autowired
    private LevelSettingsRepository levelSettingsRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public TodayProgressDto getTodayProgress(Long userId) {
        LocalDate today = LocalDate.now();
        List<com.example.kidsreading.entity.UserWordProgress> wordProgresses = wordRepo.findByUserId(userId).stream()
            .filter(wp -> wp.getIsLearned() != null && wp.getIsLearned()
                && wp.getLastLearnedAt() != null
                && wp.getLastLearnedAt().toLocalDate().isEqual(today))
            .collect(Collectors.toList());

        List<com.example.kidsreading.entity.UserSentenceProgress> sentenceProgresses = sentenceRepo.findByUserId(userId).stream()
            .filter(sp -> sp.getIsLearned() != null && sp.getIsLearned()
                && sp.getLastLearnedAt() != null
                && sp.getLastLearnedAt().toLocalDate().isEqual(today))
            .collect(Collectors.toList());

        List<TodayProgressDto.WordInfo> words = wordProgresses.stream().map(wp -> TodayProgressDto.WordInfo.builder()
            .id(wp.getWordId())
            .english(wp.getWord() != null ? wp.getWord().getText() : "")
            .korean(wp.getWord() != null ? wp.getWord().getMeaning() : "")
            .learnedAt(wp.getLastLearnedAt() != null ? wp.getLastLearnedAt().toString() : "")
            .isFavorite(Boolean.TRUE.equals(wp.getIsFavorite()))
            .build()
        ).collect(Collectors.toList());

        List<TodayProgressDto.SentenceInfo> sentences = sentenceProgresses.stream().map(sp -> TodayProgressDto.SentenceInfo.builder()
            .id(sp.getSentenceId())
            .english(sp.getSentence() != null ? sp.getSentence().getEnglishText() : "")
            .korean(sp.getSentence() != null ? sp.getSentence().getKoreanTranslation() : "")
            .learnedAt(sp.getLastLearnedAt() != null ? sp.getLastLearnedAt().toString() : "")
            .build()
        ).collect(Collectors.toList());

        int coin = words.size() + sentences.size() * 3; // 예시: 단어 1, 문장 3

        // ===== 레벨업 체크 및 처리 =====
        userRepository.findById(userId).ifPresent(user -> {
            int currentLevel = user.getLevel() != null ? user.getLevel() : 1;
            LevelSettings settings = levelSettingsRepository.findByLevel(currentLevel).orElse(null);
            if (settings != null) {
                int wordGoal = settings.getWordsToNextLevel();
                int sentenceGoal = settings.getSentencesToNextLevel();
                // 현재 레벨에서 학습 완료한 단어/문장 개수만 집계
                long learnedWords = wordRepo.findByUserIdAndLevel(userId, currentLevel).stream()
                    .filter(wp -> Boolean.TRUE.equals(wp.getIsLearned()) || Boolean.TRUE.equals(wp.getIsCompleted()))
                    .count();
                long learnedSentences = 0;
                try {
                    // sentenceRepo에 유사한 쿼리가 있으면 활용
                    learnedSentences = (long) sentenceRepo.getClass()
                        .getMethod("countByUserIdAndSentence_DifficultyLevelAndIsCompletedTrue", Long.class, Integer.class)
                        .invoke(sentenceRepo, userId, currentLevel);
                } catch (Exception e) {
                    // fallback: 전체에서 level 필터링
                    learnedSentences = sentenceRepo.findByUserId(userId).stream()
                        .filter(sp -> {
                            try {
                                return (Boolean.TRUE.equals(sp.getIsLearned()) || Boolean.TRUE.equals(sp.getIsCompleted()))
                                    && sp.getSentence() != null && sp.getSentence().getDifficultyLevel() == currentLevel;
                            } catch (Exception ex) { return false; }
                        })
                        .count();
                }
                // 로그로 확인
                System.out.println("currentLevel=" + currentLevel);
                System.out.println("wordGoal=" + wordGoal + ", sentenceGoal=" + sentenceGoal);
                System.out.println("currentLevelLearnedWords=" + learnedWords + ", currentLevelLearnedSentences=" + learnedSentences);
                if (learnedWords >= wordGoal && learnedSentences >= sentenceGoal) {
                    user.setLevel(currentLevel + 1);
                    userRepository.save(user);
                }
            }
        });
        // ===== 레벨업 체크 끝 =====

        return TodayProgressDto.builder()
            .words(words)
            .sentences(sentences)
            .coin(coin)
            .build();
    }
} 