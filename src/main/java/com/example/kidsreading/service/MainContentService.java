package com.example.kidsreading.service;

import com.example.kidsreading.dto.MainContentStatsDto;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainContentService {
    @Autowired
    private UserWordProgressRepository wordRepo;
    @Autowired
    private UserSentenceProgressRepository sentenceRepo;

    public MainContentStatsDto getStats(Long userId) {
        MainContentStatsDto dto = new MainContentStatsDto();
        dto.setTodayWords(wordRepo.countTodayCompletedWords(userId));
        dto.setTotalWords(wordRepo.countTotalCompletedWords(userId));
        dto.setTodaySentences(sentenceRepo.countTodayCompletedSentences(userId));
        dto.setTotalSentences(sentenceRepo.countTotalCompletedSentences(userId));
        // 완료된 단어/문장 ID 리스트 추가
        List<Long> completedWordIds = wordRepo.findCompletedWordIdsByUserId(userId);
        List<Long> completedSentenceIds = sentenceRepo.findCompletedSentenceIdsByUserId(userId);
        dto.setCompletedWordIds(completedWordIds);
        dto.setCompletedSentenceIds(completedSentenceIds);
        return dto;
    }
} 