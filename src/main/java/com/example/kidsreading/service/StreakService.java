package com.example.kidsreading.service;

import com.example.kidsreading.dto.UserStreakDto;
import com.example.kidsreading.entity.UserStreak;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserStreakRepository;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StreakService {

    private final UserStreakRepository userStreakRepository;
    private final UserRepository userRepository;
    private final BadgeEarningService badgeEarningService;

    /**
     * 사용자의 연속 학습일 정보 조회
     */
    public UserStreakDto getUserStreak(Long userId) {
        UserStreak userStreak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserStreak(userId));

        return convertToDto(userStreak);
    }

    /**
     * 연속 학습일 업데이트 (학습 완료 시 호출)
     */
    public UserStreakDto updateStreak(Long userId) {
        UserStreak userStreak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserStreak(userId));

        userStreak.updateStreak();
        userStreakRepository.save(userStreak);

        // 뱃지 체크
        badgeEarningService.checkBadgesOnStreakUpdate(userId);

        log.info("사용자 {}의 연속 학습일 업데이트: {}일", userId, userStreak.getCurrentStreak());
        return convertToDto(userStreak);
    }

    /**
     * 연속 학습일 리셋
     */
    public UserStreakDto resetStreak(Long userId) {
        UserStreak userStreak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserStreak(userId));

        userStreak.resetStreak();
        userStreakRepository.save(userStreak);

        log.info("사용자 {}의 연속 학습일 리셋", userId);
        return convertToDto(userStreak);
    }

    /**
     * 새로운 사용자 스트릭 생성
     */
    private UserStreak createNewUserStreak(Long userId) {
        // 사용자 이메일 가져오기
        String email = userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);

        UserStreak newStreak = UserStreak.builder()
                .userId(userId)
                .email(email)
                .currentStreak(0)
                .maxStreak(0)
                .build();
        return userStreakRepository.save(newStreak);
    }

    /**
     * DTO 변환
     */
    private UserStreakDto convertToDto(UserStreak userStreak) {
        String streakMessage = generateStreakMessage(userStreak.getCurrentStreak());
        String nextGoalMessage = generateNextGoalMessage(userStreak.getCurrentStreak(), userStreak.getDaysToNextGoal());

        return UserStreakDto.builder()
                .userId(userStreak.getUserId())
                .email(userStreak.getEmail())
                .currentStreak(userStreak.getCurrentStreak())
                .maxStreak(userStreak.getMaxStreak())
                .lastLearningDate(userStreak.getLastLearningDate())
                .streakStartDate(userStreak.getStreakStartDate())
                .isLearnedToday(userStreak.isLearnedToday())
                .isOnStreak(userStreak.isOnStreak())
                .streakBonus(userStreak.calculateStreakBonus())
                .streakProgress(userStreak.getStreakProgress())
                .daysToNextGoal(userStreak.getDaysToNextGoal())
                .currentGoal(userStreak.getCurrentGoal())
                .streakMessage(streakMessage)
                .nextGoalMessage(nextGoalMessage)
                .build();
    }

    /**
     * 연속 학습일 메시지 생성
     */
    private String generateStreakMessage(int currentStreak) {
        if (currentStreak == 0) {
            return "오늘부터 시작해보세요!";
        } else if (currentStreak == 1) {
            return "첫 번째 학습 완료!";
        } else if (currentStreak < 7) {
            return currentStreak + "일 연속 학습 중!";
        } else if (currentStreak < 30) {
            return currentStreak + "일 연속! 대단해요!";
        } else if (currentStreak < 100) {
            return currentStreak + "일 연속! 정말 멋져요!";
        } else {
            return currentStreak + "일 연속! 전설적인 학습자!";
        }
    }

    /**
     * 다음 목표 메시지 생성
     */
    private String generateNextGoalMessage(int currentStreak, int daysToNextGoal) {
        if (daysToNextGoal == 0) {
            return "모든 목표를 달성했습니다!";
        } else if (daysToNextGoal == 1) {
            return "내일 학습하면 다음 목표 달성!";
        } else {
            return daysToNextGoal + "일 더 학습하면 다음 목표 달성!";
        }
    }

    /**
     * 오늘 학습했는지 확인
     */
    public boolean isLearnedToday(Long userId) {
        Optional<UserStreak> userStreak = userStreakRepository.findByUserId(userId);
        return userStreak.map(UserStreak::isLearnedToday).orElse(false);
    }

    /**
     * 연속 학습일 보너스 계산
     */
    public int calculateStreakBonus(Long userId) {
        UserStreak userStreak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserStreak(userId));
        return userStreak.calculateStreakBonus();
    }

    /**
     * 기존 연속 학습일 데이터에 이메일 업데이트
     */
    @Transactional
    public void updateEmailForExistingStreaks() {
        List<UserStreak> streaks = userStreakRepository.findAll();
        for (UserStreak streak : streaks) {
            if (streak.getEmail() == null) {
                String email = userRepository.findById(streak.getUserId())
                        .map(User::getEmail)
                        .orElse(null);
                streak.setEmail(email);
                userStreakRepository.save(streak);
            }
        }
        log.info("기존 연속 학습일 데이터에 이메일 업데이트 완료: {}개", streaks.size());
    }
} 