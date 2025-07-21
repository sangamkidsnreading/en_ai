package com.example.kidsreading.service;

import com.example.kidsreading.dto.UserBadgeDto;
import com.example.kidsreading.entity.BadgeSettings;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.entity.UserBadge;
import com.example.kidsreading.repository.BadgeSettingsRepository;
import com.example.kidsreading.repository.UserBadgeRepository;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final BadgeSettingsRepository badgeSettingsRepository;

    /**
     * 사용자의 모든 뱃지 조회
     */
    public List<UserBadgeDto> getUserBadges(Long userId) {
        return userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId).stream()
                .filter(ub -> ub.getBadge().getIsActive() != null && ub.getBadge().getIsActive())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 표시 가능한 뱃지 조회
     */
    public List<UserBadgeDto> getDisplayedUserBadges(Long userId) {
        return userBadgeRepository.findByUserIdAndIsDisplayedTrueOrderByEarnedAtDesc(userId).stream()
                .filter(ub -> ub.getBadge().getIsActive() != null && ub.getBadge().getIsActive())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 최근 획득 뱃지 조회 (최대 5개)
     */
    public List<UserBadgeDto> getRecentUserBadges(Long userId) {
        return userBadgeRepository.findRecentBadgesByUserId(userId, PageRequest.of(0, 5)).stream()
                .filter(ub -> ub.getBadge().getIsActive() != null && ub.getBadge().getIsActive())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 뱃지 획득
     */
    public UserBadgeDto earnBadge(Long userId, Long badgeId) {
        // 이미 획득한 뱃지인지 확인
        Optional<UserBadge> existingBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId);
        if (existingBadge.isPresent()) {
            return convertToDto(existingBadge.get());
        }

        // 사용자와 뱃지 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        BadgeSettings badge = badgeSettingsRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("뱃지를 찾을 수 없습니다: " + badgeId));

        // 뱃지 획득
        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .earnedAt(LocalDateTime.now())
                .isDisplayed(true)
                .email(user.getEmail())
                .build();

        UserBadge saved = userBadgeRepository.save(userBadge);
        return convertToDto(saved);
    }

    /**
     * 뱃지 표시/숨김 토글
     */
    public UserBadgeDto toggleBadgeDisplay(Long userBadgeId) {
        UserBadge userBadge = userBadgeRepository.findById(userBadgeId)
                .orElseThrow(() -> new RuntimeException("사용자 뱃지를 찾을 수 없습니다: " + userBadgeId));

        userBadge.setIsDisplayed(!userBadge.getIsDisplayed());
        userBadge.setUpdatedAt(LocalDateTime.now());

        UserBadge saved = userBadgeRepository.save(userBadge);
        return convertToDto(saved);
    }

    /**
     * 뱃지 삭제
     */
    public void deleteUserBadge(Long userBadgeId) {
        userBadgeRepository.deleteById(userBadgeId);
    }

    /**
     * 사용자가 획득한 뱃지 개수
     */
    public long getUserBadgeCount(Long userId) {
        return userBadgeRepository.countByUserId(userId);
    }

    /**
     * 사용자가 표시 가능한 뱃지 개수
     */
    public long getDisplayedUserBadgeCount(Long userId) {
        return userBadgeRepository.countByUserIdAndIsDisplayedTrue(userId);
    }

    /**
     * 특정 뱃지를 획득한 사용자 수
     */
    public long getBadgeEarnedCount(Long badgeId) {
        return userBadgeRepository.countByBadgeId(badgeId);
    }

    /**
     * 사용자가 특정 뱃지를 획득했는지 확인
     */
    public boolean hasUserEarnedBadge(Long userId, Long badgeId) {
        return userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId).isPresent();
    }

    /**
     * Entity를 DTO로 변환
     */
    private UserBadgeDto convertToDto(UserBadge entity) {
        return UserBadgeDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .badgeId(entity.getBadge().getId())
                .badgeName(entity.getBadge().getBadgeName())
                .badgeIcon(entity.getBadge().getBadgeIcon())
                .badgeDescription(entity.getBadge().getBadgeDescription())
                .earnedAt(entity.getEarnedAt())
                .isDisplayed(entity.getIsDisplayed())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 