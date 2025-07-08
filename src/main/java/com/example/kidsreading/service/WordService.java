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
import java.util.stream.IntStream;

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
     * Word 엔티티를 DTO로 변환
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
                .updatedAt(word.getUpdatedAt())
                .build();
    }

    public List<Integer> getAvailableLevels() {
        List<Integer> levels = wordRepository.findDistinctLevelsByIsActiveTrueOrderByLevel();
        if (levels.isEmpty()) {
            return List.of(1, 2, 3, 4, 5);
        }
        // 레벨 1-5로 제한
        return levels.stream()
                .filter(level -> level >= 1 && level <= 5)
                .collect(Collectors.toList());
    }

    public List<Integer> getAvailableDaysByLevel(Integer level) {
        List<Integer> days = wordRepository.findDistinctDaysByLevelAndIsActiveTrueOrderByDay(level);
        if (days.isEmpty()) {
            return IntStream.rangeClosed(1, 50).boxed().toList();
        }
        // Day 1-50으로 제한
        return days.stream()
                .filter(day -> day >= 1 && day <= 50)
                .collect(Collectors.toList());
    }

    /**
     * 단어 학습 진행상황 업데이트
     */
    @Transactional
    public void updateWordProgress(Long userId, Long wordId, Boolean isCompleted) {
        UserWordProgress progress = userWordProgressRepository
                .findByUserIdAndWordId(userId, wordId)
                .orElse(UserWordProgress.builder()
                        .userId(userId)
                        .wordId(wordId)
                        .isCompleted(false)
                        .build());

        progress.setIsCompleted(isCompleted);
        userWordProgressRepository.save(progress);
    }

    /**
     * 사용자의 단어 완료 개수 조회
     */
    public int getCompletedWordsCount(Long userId, Integer level, Integer day) {
        if (level == 0 && day == 0) {
            return (int) userWordProgressRepository.countByUserIdAndIsCompletedTrue(userId);
        } else if (level == 0) {
            return userWordProgressRepository.countCompletedWordsByUserAndDay(userId, day);
        } else if (day == 0) {
            return userWordProgressRepository.countCompletedWordsByUserAndLevel(userId, level);
        } else {
            return userWordProgressRepository.countCompletedWordsByUserAndLevelAndDay(userId, level, day);
        }
    }

    /**
     * 사용 가능한 레벨 목록 조회
     */
    public List<Integer> getAvailableLevels() {
        List<Integer> levels = wordRepository.findDistinctLevels();
        if (levels.isEmpty()) {
            return List.of(1, 2, 3, 4, 5);
        }
        // 레벨 1-5로 제한
        return levels.stream()
                .filter(level -> level >= 1 && level <= 5)
                .collect(Collectors.toList());
    }

    /**
     * 특정 레벨의 사용 가능한 Day 목록 조회
     */
    public List<Integer> getAvailableDaysByLevel(Integer level) {
        List<Integer> days = wordRepository.findDistinctDaysByLevel(level);
        if (days.isEmpty()) {
            return IntStream.rangeClosed(1, 50).boxed().toList();
        }
        // Day 1-50으로 제한
        return days.stream()
                .filter(day -> day >= 1 && day <= 50)
                .collect(Collectors.toList());
    }

    /**
     * 전체 단어 수 조회
     */
    public int getTotalWordsCount(Integer level, Integer day) {
        // 기존: countByLevelAndDayAndIsActive(level, day, true);
        // 변경:
        return wordRepository.countByLevelAndDayAndIsActiveTrue(level, day);
    }

    public List<Word> getWordsByLevel(Integer level) {
        return wordRepository.findByLevelAndIsActiveTrueOrderByDayAsc(level);
    }

    /**
     * 획득한 코인 수 계산
     */
    public int getCoinsEarned(Long userId, Integer level, Integer day) {
        int completedWords = getCompletedWordsCount(userId, level, day);
        return completedWords * 10; // 단어 하나당 10코인
    }

    /**
     * 단어 즐겨찾기 토글
     */
    @Transactional
    public boolean toggleWordFavorite(Long userId, Long wordId) {
        UserWordProgress progress = userWordProgressRepository
                .findByUserIdAndWordId(userId, wordId)
                .orElse(UserWordProgress.builder()
                        .userId(userId)
                        .wordId(wordId)
                        .isFavorite(false)
                        .build());

        progress.setIsFavorite(!progress.getIsFavorite());
        userWordProgressRepository.save(progress);

        return progress.getIsFavorite();
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

    // 사이드바용 - 사용 가능한 레벨 목록
    public List<Integer> getAvailableLevels() {
        List<Integer> levels = wordRepository.findAvailableLevels();
        if (levels.isEmpty()) {
            return List.of(1, 2, 3, 4, 5);
        }
        // 레벨 1-5로 제한
        return levels.stream()
                .filter(level -> level >= 1 && level <= 5)
                .collect(Collectors.toList());
    }

    // 사이드바용 - 특정 레벨의 사용 가능한 Day 목록
    public List<Integer> getAvailableDaysByLevel(Integer level) {
        List<Integer> days = wordRepository.findAvailableDaysByLevel(level);
        if (days.isEmpty()) {
            return IntStream.rangeClosed(1, 50).boxed().toList();
        }
        // Day 1-50으로 제한
        return days.stream()
                .filter(day -> day >= 1 && day <= 50)
                .collect(Collectors.toList());
    }
}