package com.example.kidsreading.service;

import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.entity.Word;
import com.example.kidsreading.entity.UserWordProgress;
import com.example.kidsreading.repository.WordRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordService {

    private final WordRepository wordRepository;
    private final UserWordProgressRepository userWordProgressRepository;

    /**
     * 특정 레벨과 날짜의 단어 목록 조회
     */
    public List<WordDto> getWordsByLevelAndDay(Integer level, Integer day) {
        List<Word> words;

        if (level == 0 && day == 0) {
            // 모든 레벨, 모든 Day
            words = wordRepository.findByIsActiveTrueOrderByLevelAscDayAsc();
        } else if (level == 0) {
            // 모든 레벨, 특정 Day
            words = wordRepository.findByDayAndIsActiveTrueOrderByLevelAsc(day);
        } else if (day == 0) {
            // 특정 레벨, 모든 Day
            words = wordRepository.findByLevelAndIsActiveTrueOrderByDayAsc(level);
        } else {
            // 특정 레벨, 특정 Day
            words = wordRepository.findByLevelAndDayAndIsActiveTrue(level, day);
        }

        return words.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 단어들의 고유 레벨 목록 조회
     */
    public List<Integer> getAvailableLevels() {
        return wordRepository.findDistinctLevelsByIsActiveTrueOrderByLevel();
    }

    /**
     * 특정 레벨의 활성화된 단어들의 고유 날짜 목록 조회
     */
    public List<Integer> getAvailableDaysByLevel(Integer level) {
        return wordRepository.findDistinctDaysByLevelAndIsActiveTrueOrderByDay(level);
    }

    /**
     * 특정 레벨과 날짜의 활성화된 단어 개수 조회
     */
    public int getWordCountByLevelAndDay(Integer level, Integer day) {
        return wordRepository.countByLevelAndDayAndIsActiveTrue(level, day);
    }

    /**
     * 사용자의 단어 학습 진행상황 조회
     */
    public List<UserWordProgress> getUserWordProgress(String userId, Integer level, Integer day) {
        return userWordProgressRepository.findByUserIdAndLevelAndDay(userId, level, day);
    }

    /**
     * Word 엔티티를 WordDto로 변환
     */
    private WordDto convertToDto(Word word) {
        return WordDto.builder()
                .id(word.getId())
                .text(word.getText())
                .meaning(word.getMeaning())
                .pronunciation(word.getPronunciation())
                .level(word.getLevel())
                .day(word.getDay())
                .audioUrl(word.getAudioUrl())
                .isActive(word.getIsActive())
                .createdAt(word.getCreatedAt())
                .english(word.getText()) // 호환성을 위한 매핑
                .korean(word.getMeaning()) // 호환성을 위한 매핑
                .build();
    }
}