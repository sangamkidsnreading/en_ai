package com.example.kidsreading.service;

import com.example.kidsreading.dto.BadgeSettingsDto;
import com.example.kidsreading.entity.BadgeSettings;
import com.example.kidsreading.repository.BadgeSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BadgeSettingsService {

    @Autowired
    private BadgeSettingsRepository badgeSettingsRepository;

    /**
     * 모든 뱃지 설정 조회
     */
    public List<BadgeSettingsDto> getAllBadgeSettings() {
        return badgeSettingsRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 뱃지 설정 조회
     */
    public List<BadgeSettingsDto> getActiveBadgeSettings() {
        return badgeSettingsRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타입의 뱃지 설정 조회 (출석)
     */
    public List<BadgeSettingsDto> getAttendanceBadgeSettings() {
        return badgeSettingsRepository.findByAttendanceCountIsNotNullAndIsActiveTrueOrderByAttendanceCountAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타입의 뱃지 설정 조회 (연속출석)
     */
    public List<BadgeSettingsDto> getStreakBadgeSettings() {
        return badgeSettingsRepository.findByStreakCountIsNotNullAndIsActiveTrueOrderByStreakCountAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타입의 뱃지 설정 조회 (단어)
     */
    public List<BadgeSettingsDto> getWordsBadgeSettings() {
        return badgeSettingsRepository.findByWordsCountIsNotNullAndIsActiveTrueOrderByWordsCountAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타입의 뱃지 설정 조회 (문장)
     */
    public List<BadgeSettingsDto> getSentencesBadgeSettings() {
        return badgeSettingsRepository.findBySentencesCountIsNotNullAndIsActiveTrueOrderBySentencesCountAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타입의 뱃지 설정 조회 (복습단어)
     */
    public List<BadgeSettingsDto> getWordReviewBadgeSettings() {
        return badgeSettingsRepository.findByWordReviewCountIsNotNullAndIsActiveTrueOrderByWordReviewCountAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타입의 뱃지 설정 조회 (복습문장)
     */
    public List<BadgeSettingsDto> getSentenceReviewBadgeSettings() {
        return badgeSettingsRepository.findBySentenceReviewCountIsNotNullAndIsActiveTrueOrderBySentenceReviewCountAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 뱃지 설정 생성
     */
    public BadgeSettingsDto createBadgeSettings(BadgeSettingsDto dto) {
        BadgeSettings badgeSettings = BadgeSettings.builder()
                .badgeName(dto.getBadgeName())
                .badgeIcon(dto.getBadgeIcon())
                .badgeDescription(dto.getBadgeDescription())
                .attendanceCount(dto.getAttendanceCount())
                .streakCount(dto.getStreakCount())
                .wordsCount(dto.getWordsCount())
                .sentencesCount(dto.getSentencesCount())
                .wordReviewCount(dto.getWordReviewCount())
                .sentenceReviewCount(dto.getSentenceReviewCount())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .createdAt(LocalDateTime.now())
                .build();

        BadgeSettings saved = badgeSettingsRepository.save(badgeSettings);
        return convertToDto(saved);
    }

    /**
     * 뱃지 설정 수정
     */
    public BadgeSettingsDto updateBadgeSettings(Long id, BadgeSettingsDto dto) {
        BadgeSettings badgeSettings = badgeSettingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("뱃지 설정을 찾을 수 없습니다: " + id));

        if (dto.getBadgeName() != null) {
            badgeSettings.setBadgeName(dto.getBadgeName());
        }
        if (dto.getBadgeIcon() != null) {
            badgeSettings.setBadgeIcon(dto.getBadgeIcon());
        }
        badgeSettings.setBadgeDescription(dto.getBadgeDescription());
        badgeSettings.setAttendanceCount(dto.getAttendanceCount());
        badgeSettings.setStreakCount(dto.getStreakCount());
        badgeSettings.setWordsCount(dto.getWordsCount());
        badgeSettings.setSentencesCount(dto.getSentencesCount());
        badgeSettings.setWordReviewCount(dto.getWordReviewCount());
        badgeSettings.setSentenceReviewCount(dto.getSentenceReviewCount());
        badgeSettings.setIsActive(dto.getIsActive());
        badgeSettings.setDisplayOrder(dto.getDisplayOrder());
        badgeSettings.setUpdatedAt(LocalDateTime.now());

        BadgeSettings saved = badgeSettingsRepository.save(badgeSettings);
        return convertToDto(saved);
    }

    /**
     * 뱃지 설정 삭제
     */
    public void deleteBadgeSettings(Long id) {
        badgeSettingsRepository.deleteById(id);
    }

    /**
     * 뱃지 설정 활성화/비활성화 토글
     */
    public BadgeSettingsDto toggleBadgeSettings(Long id) {
        BadgeSettings badgeSettings = badgeSettingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("뱃지 설정을 찾을 수 없습니다: " + id));

        badgeSettings.setIsActive(!badgeSettings.getIsActive());
        badgeSettings.setUpdatedAt(LocalDateTime.now());

        BadgeSettings saved = badgeSettingsRepository.save(badgeSettings);
        return convertToDto(saved);
    }

    /**
     * 기본 뱃지 설정 초기화
     */
    public void initializeDefaultBadgeSettings() {
        if (badgeSettingsRepository.count() == 0) {
            // 첫 걸음 뱃지 (출석)
            badgeSettingsRepository.save(BadgeSettings.builder()
                    .badgeName("첫 걸음")
                    .badgeIcon("🎯")
                    .badgeDescription("첫 번째 학습을 완료했습니다")
                    .attendanceCount(1)
                    .isActive(true)
                    .displayOrder(1)
                    .createdAt(LocalDateTime.now())
                    .build());

            // 열정 학습자 뱃지 (연속출석)
            badgeSettingsRepository.save(BadgeSettings.builder()
                    .badgeName("열정 학습자")
                    .badgeIcon("🔥")
                    .badgeDescription("연속 출석을 달성했습니다")
                    .streakCount(7)
                    .isActive(true)
                    .displayOrder(2)
                    .createdAt(LocalDateTime.now())
                    .build());

            // 단어 마스터 뱃지 (단어)
            badgeSettingsRepository.save(BadgeSettings.builder()
                    .badgeName("단어 마스터")
                    .badgeIcon("📚")
                    .badgeDescription("단어 학습을 달성했습니다")
                    .wordsCount(100)
                    .isActive(true)
                    .displayOrder(3)
                    .createdAt(LocalDateTime.now())
                    .build());

            // 골드 마스터 뱃지 (문장)
            badgeSettingsRepository.save(BadgeSettings.builder()
                    .badgeName("골드 마스터")
                    .badgeIcon("🏆")
                    .badgeDescription("문장 학습을 달성했습니다")
                    .sentencesCount(50)
                    .isActive(true)
                    .displayOrder(4)
                    .createdAt(LocalDateTime.now())
                    .build());

            // 전설 수집가 뱃지 (복습단어)
            badgeSettingsRepository.save(BadgeSettings.builder()
                    .badgeName("전설 수집가")
                    .badgeIcon("⭐")
                    .badgeDescription("복습을 달성했습니다")
                    .wordReviewCount(200)
                    .isActive(true)
                    .displayOrder(5)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * Entity를 DTO로 변환
     */
    private BadgeSettingsDto convertToDto(BadgeSettings entity) {
        return BadgeSettingsDto.builder()
                .id(entity.getId())
                .badgeName(entity.getBadgeName())
                .badgeIcon(entity.getBadgeIcon())
                .badgeDescription(entity.getBadgeDescription())
                .attendanceCount(entity.getAttendanceCount())
                .streakCount(entity.getStreakCount())
                .wordsCount(entity.getWordsCount())
                .sentencesCount(entity.getSentencesCount())
                .wordReviewCount(entity.getWordReviewCount())
                .sentenceReviewCount(entity.getSentenceReviewCount())
                .isActive(entity.getIsActive())
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 