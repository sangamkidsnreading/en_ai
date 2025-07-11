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

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
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

        List<WordDto> wordDtos = words.stream()
                .map(word -> {
                    WordDto dto = convertToDto(word);
                    // 오디오 URL 유효성 검증 및 수정
                    if (dto.getAudioUrl() != null && !dto.getAudioUrl().isEmpty()) {
                        String audioUrl = dto.getAudioUrl();
                        // S3 URL이 아닌 경우 로컬 경로로 변경
                        if (!audioUrl.contains("amazonaws.com")) {
                            // 파일명만 있는 경우 적절한 경로로 변경
                            if (!audioUrl.startsWith("/audio/words/")) {
                                audioUrl = "/audio/words/" + audioUrl;
                            }
                            dto.setAudioUrl(audioUrl);
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Level {} Day {} 단어 조회 완료: {}개", level, day, wordDtos.size());
        return wordDtos;
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
    public List<UserWordProgress> getUserWordProgress(Long userId, Integer level, Integer day) {
        return userWordProgressRepository.findByUserIdAndLevel(userId, level);
    }

    /**
     * 단어 학습 진행상황 업데이트 (Upsert: insert or update)
     */
    @Transactional
    public void updateWordProgress(Long userId, Long wordId, Boolean isCompleted, String email) {
        UserWordProgress progress = userWordProgressRepository
                .findByUserIdAndWordId(userId, wordId)
                .orElseGet(() -> {
                    UserWordProgress newProgress = UserWordProgress.builder()
                            .userId(userId)
                            .wordId(wordId)
                            .isCompleted(false)
                            .createdAt(java.time.LocalDateTime.now())
                            .email(email)
                            .build();
                    // 최초 학습 시각 기록
                    newProgress.setFirstLearnedAt(java.time.LocalDateTime.now());
                    return newProgress;
                });

        // 상태 갱신
        progress.setIsCompleted(isCompleted);
        progress.setIsLearned(isCompleted != null && isCompleted);
        progress.setLastLearnedAt(java.time.LocalDateTime.now());
        progress.setUpdatedAt(java.time.LocalDateTime.now());
        progress.setEmail(email);
        if (progress.getFirstLearnedAt() == null) {
            progress.setFirstLearnedAt(java.time.LocalDateTime.now());
        }
        userWordProgressRepository.save(progress);
    }

    /**
     * 완료된 단어 수 조회
     */
    public int getCompletedWordsCount(Long userId, Integer level, Integer day) {
        return userWordProgressRepository.countCompletedWordsByUserAndLevelAndDay(userId, level, day);
    }

    /**
     * 전체 단어 수 조회
     */
    public int getTotalWordsCount(Integer level, Integer day) {
        return wordRepository.countByLevelAndDayAndIsActiveTrue(level, day);
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
}