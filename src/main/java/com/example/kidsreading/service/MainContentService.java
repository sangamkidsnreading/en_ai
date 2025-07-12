package com.example.kidsreading.service;

import com.example.kidsreading.dto.MainContentStatsDto;
import com.example.kidsreading.dto.TodayProgressDto;
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

        return TodayProgressDto.builder()
            .words(words)
            .sentences(sentences)
            .coin(coin)
            .build();
    }
} 