package com.example.kidsreading.service;

import com.example.kidsreading.dto.LearningSettingsDto;
import com.example.kidsreading.dto.UserCoinsDto;
import com.example.kidsreading.entity.UserCoin;
import com.example.kidsreading.repository.UserCoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.example.kidsreading.entity.LearningSettings;
import com.example.kidsreading.repository.LearningSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoinService {

    private final UserCoinRepository userCoinRepository;
    private final AdminService adminService;
    @Autowired
    private LearningSettingsRepository learningSettingsRepository;

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName(); // 이메일 또는 username
        }
        return "anonymous"; // 기본값
    }

    public UserCoinsDto getUserCoins(String userId) {
        LocalDate today = LocalDate.now();
        userId = userId.trim().toLowerCase(); // 정규화
        Optional<UserCoin> userCoinOpt = userCoinRepository.findByUserIdAndDate(userId, today);

        if (userCoinOpt.isPresent()) {
            UserCoin userCoin = userCoinOpt.get();
            return convertToDto(userCoin);
        } else {
            // 오늘 데이터가 없으면 새로 생성
            UserCoin newUserCoin = UserCoin.builder()
                    .userId(userId)
                    .date(today)
                    .wordsCoins(0)
                    .sentenceCoins(0)
                    .streakBonus(0)
                    .totalDailyCoins(0)
                    .coinsEarned(0)
                    .dailyCoins(0)
                    .totalCoins(getTotalCoins(userId))
                    // createdAt, lastUpdated는 빌더에 넣지 않는다!
                    .build();

            userCoinRepository.save(newUserCoin);
            return convertToDto(newUserCoin);
        }
    }

    public UserCoinsDto addWordCoins(String userId, Integer coins) {
        LocalDate today = LocalDate.now();
        userId = userId.trim().toLowerCase(); // 정규화
        UserCoin userCoin = userCoinRepository.findByUserIdAndDate(userId, today)
                .orElse(UserCoin.builder()
                        .userId(userId)
                        .date(today)
                        .wordsCoins(0)
                        .sentenceCoins(0)
                        .streakBonus(0)
                        .totalDailyCoins(0)
                        .coinsEarned(0)
                        .dailyCoins(0)
                        .totalCoins(getTotalCoins(userId))
                        // createdAt, lastUpdated는 빌더에 넣지 않는다!
                        .build());

        userCoin.addWordCoins(coins);
        userCoinRepository.save(userCoin);

        log.info("사용자 {}에게 단어 학습 코인 {} 추가", userId, coins);
        return convertToDto(userCoin);
    }

    public UserCoinsDto addSentenceCoins(String userId, Integer coins) {
        LocalDate today = LocalDate.now();
        userId = userId.trim().toLowerCase(); // 정규화
        UserCoin userCoin = userCoinRepository.findByUserIdAndDate(userId, today)
                .orElse(UserCoin.builder()
                        .userId(userId)
                        .date(today)
                        .wordsCoins(0)
                        .sentenceCoins(0)
                        .streakBonus(0)
                        .totalDailyCoins(0)
                        .coinsEarned(0)
                        .dailyCoins(0)
                        .totalCoins(getTotalCoins(userId))
                        // createdAt, lastUpdated는 빌더에 넣지 않는다!
                        .build());

        userCoin.addSentenceCoins(coins);
        userCoinRepository.save(userCoin);

        log.info("사용자 {}에게 문장 학습 코인 {} 추가", userId, coins);
        return convertToDto(userCoin);
    }

    public UserCoinsDto addStreakBonus(String userId, Integer bonus) {
        LocalDate today = LocalDate.now();
        userId = userId.trim().toLowerCase(); // 정규화
        UserCoin userCoin = userCoinRepository.findByUserIdAndDate(userId, today)
                .orElse(UserCoin.builder()
                        .userId(userId)
                        .date(today)
                        .wordsCoins(0)
                        .sentenceCoins(0)
                        .streakBonus(0)
                        .totalDailyCoins(0)
                        .coinsEarned(0)
                        .dailyCoins(0)
                        .totalCoins(getTotalCoins(userId))
                        // createdAt, lastUpdated는 빌더에 넣지 않는다!
                        .build());

        userCoin.addStreakBonus(bonus);
        userCoinRepository.save(userCoin);

        log.info("사용자 {}에게 연속 학습 보너스 {} 추가", userId, bonus);
        return convertToDto(userCoin);
    }

    public Integer getTotalCoins(String userId) {
        userId = userId.trim().toLowerCase(); // 정규화
        Integer total = userCoinRepository.getTotalCoinsByUserId(userId);
        return total != null ? total : 0;
    }

    public List<UserCoinsDto> getUserCoinHistory(String userId, int days) {
        userId = userId.trim().toLowerCase(); // 정규화
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        return userCoinRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public LearningSettingsDto getCoinSettings() {
        try {
            LearningSettings latest = learningSettingsRepository.findTopByOrderByCreatedAtDesc().orElse(null);
            if (latest != null) {
                return LearningSettingsDto.builder()
                        .wordCoin(latest.getWordCoin())
                        .sentenceCoin(latest.getSentenceCoin())
                        .streakBonus(latest.getStreakBonus())
                        .levelUpCoin(latest.getLevelUpCoin())
                        .wordCoins(latest.getWordCoins())
                        .sentenceCoins(latest.getSentenceCoins())
                        .maxWordsPerDay(latest.getMaxWordsPerDay())
                        .maxSentencesPerDay(latest.getMaxSentencesPerDay())
                        .difficultyLevel(latest.getDifficultyLevel())
                        .enableAudio(latest.getEnableAudio())
                        .enableFeedback(latest.getEnableFeedback())
                        .audioSpeed(latest.getAudioSpeed())
                        .voiceSpeed(latest.getVoiceSpeed())
                        .repeatCount(latest.getRepeatCount())
                        .maxLevel(latest.getMaxLevel())
                        .dailyWordGoal(latest.getDailyWordGoal())
                        .dailySentenceGoal(latest.getDailySentenceGoal())
                        .voiceEnabled(latest.getVoiceEnabled())
                        .voiceLanguage(latest.getVoiceLanguage())
                        .dailyGoal(latest.getDailyGoal())
                        .soundEffects(latest.getSoundEffects())
                        .autoPlay(latest.getAutoPlay())
                        .darkMode(latest.getDarkMode())
                        .build();
            }
            return LearningSettingsDto.builder()
                    .wordCoin(1)
                    .sentenceCoin(3)
                    .streakBonus(5)
                    .levelUpCoin(100)
                    .wordCoins(1)
                    .sentenceCoins(3)
                    .build();
        } catch (Exception e) {
            log.warn("코인 설정 로드 실패, 기본값 사용: {}", e.getMessage());
            return LearningSettingsDto.builder()
                    .wordCoin(1)
                    .sentenceCoin(3)
                    .streakBonus(5)
                    .levelUpCoin(100)
                    .wordCoins(1)
                    .sentenceCoins(3)
                    .build();
        }
    }

    private UserCoinsDto convertToDto(UserCoin userCoin) {
        return UserCoinsDto.builder()
                .id(userCoin.getId())
                .userId(userCoin.getUserId())
                .date(userCoin.getDate().toString())
                .wordsCoins(userCoin.getWordsCoins())
                .sentenceCoins(userCoin.getSentenceCoins())
                .streakBonus(userCoin.getStreakBonus())
                .totalDailyCoins(userCoin.getTotalDailyCoins())
                .coinsEarned(userCoin.getCoinsEarned())
                .dailyCoins(userCoin.getDailyCoins())
                .totalCoins(userCoin.getTotalCoins())
                .createdAt(userCoin.getCreatedAt())
                .lastUpdated(userCoin.getLastUpdated())
                .build();
    }

    // API 엔드포인트용 메서드들
    public UserCoinsDto getCurrentUserCoins() {
        String userId = getCurrentUserId();
        return getUserCoins(userId);
    }

    public UserCoinsDto addCurrentUserWordCoins() {
        String userId = getCurrentUserId();
        LearningSettingsDto settings = getCoinSettings();
        int wordReward = settings.getWordCoins() != null ? settings.getWordCoins() : 1;
        return addWordCoins(userId, wordReward);
    }

    public UserCoinsDto addCurrentUserSentenceCoins() {
        String userId = getCurrentUserId();
        LearningSettingsDto settings = getCoinSettings();
        int sentenceReward = settings.getSentenceCoins() != null ? settings.getSentenceCoins() : 3;
        return addSentenceCoins(userId, sentenceReward);
    }

    public UserCoinsDto addCurrentUserStreakBonus() {
        String userId = getCurrentUserId();
        LearningSettingsDto settings = getCoinSettings();
        return addStreakBonus(userId, settings.getStreakBonus());
    }
}