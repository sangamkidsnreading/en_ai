// ========== UserService.java (업데이트) ==========
package com.example.kidsreading.service;

import com.example.kidsreading.dto.LevelProgressDto;
import com.example.kidsreading.dto.RegisterRequest;
import com.example.kidsreading.entity.LevelSettings;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.entity.UserLevels;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.LevelSettingsRepository;
import com.example.kidsreading.repository.UserLevelsRepository;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserWordProgressRepository userWordProgressRepository;
    private final LevelSettingsRepository levelSettingsRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;
    private final UserLevelsRepository userLevelsRepository;

    /**
     * 현재 로그인한 사용자 조회
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByEmail(email).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 회원가입
     */
    public User registerUser(RegisterRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // 사용자명 중복 확인 (이메일과 동일하므로 이메일 체크로 대체)
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 사용자명입니다.");
        }

        // 연락처 중복 확인
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 사용중인 연락처입니다.");
        }

        // User 엔티티 생성
        User user = User.builder()
                .username(request.getUsername()) // 이메일과 동일
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .parentName(request.getParentName())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .groupName(request.getGroupName())
                .role(request.getRole())
                .isActive(request.getIsActive())
                .emailVerified(request.getEmailVerified())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                // 동의 항목 매핑 추가
                .agreeTerms(request.getAgreeTerms() != null ? request.getAgreeTerms() : false)
                .agreePrivacy(request.getAgreePrivacy() != null ? request.getAgreePrivacy() : false)
                .agreeMarketing(request.getAgreeMarketing() != null ? request.getAgreeMarketing() : false)
                .build();

        User savedUser = userRepository.save(user);
        
        // UserLevels 엔티티 생성 (기본값: 레벨 1, 코인 0, 경험치 0)
        UserLevels userLevels = UserLevels.builder()
                .userId(savedUser.getId())
                .currentLevel(1)
                .currentDay(1)
                .totalCoins(0)
                .experiencePoints(0)
                .streakDays(0)
                .build();
        
        userLevelsRepository.save(userLevels);
        
        log.info("회원가입 완료: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        return savedUser;
    }

    /**
     * 이메일로 사용자 조회
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자명으로 사용자 조회
     */
    public Optional<User> findByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getIsActive() != null && !user.getIsActive()) {
                throw new IllegalStateException("비승인 상태입니다. 관리자에게 문의 바랍니다.");
            }
        }
        return userOpt;
    }

    /**
     * 이메일 인증번호 전송 (나중에 구현)
     */
    public void sendVerificationCode(String email) {
        // 현재는 이메일 인증 비활성화
        throw new UnsupportedOperationException("이메일 인증 기능이 현재 비활성화되어 있습니다.");
    }

    /**
     * 인증번호 확인 (나중에 구현)
     */
    public boolean verifyCode(String email, String code) {
        // 현재는 이메일 인증 비활성화
        throw new UnsupportedOperationException("이메일 인증 기능이 현재 비활성화되어 있습니다.");
    }

    /**
     * 사용자 활성화/비활성화
     */
    public void updateUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setIsActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("사용자 상태 변경: {} (ID: {}) -> {}",
                user.getEmail(), user.getId(), isActive ? "활성화" : "비활성화");
    }

    /**
     * 이메일 인증 상태 업데이트
     */
    public void updateEmailVerified(Long userId, boolean verified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setEmailVerified(verified);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("이메일 인증 상태 변경: {} (ID: {}) -> {}",
                user.getEmail(), user.getId(), verified ? "인증됨" : "인증 안됨");
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    /**
     * 사용자 정보 조회 (ID로)
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("비밀번호 변경 완료: {} (ID: {})", user.getEmail(), user.getId());
    }

    public LevelProgressDto getLevelProgressForCurrentUser() {
        User user = getCurrentUser();
        return getLevelProgressForUser(user.getId());
    }

    public LevelProgressDto getLevelProgressForUser(Long userId) {
        // UserLevels에서 현재 레벨 정보 조회
        UserLevels userLevels = userLevelsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultUserLevels(userId));
        
        int currentLevel = userLevels.getCurrentLevel();
        
        // user_word_progress와 user_sentence_progress에서 학습 완료된 개수 조회
        int learnedWords = userWordProgressRepository.countByUserIdAndIsLearnedTrue(userId);
        int learnedSentences = userSentenceProgressRepository.countByUserIdAndIsLearnedTrue(userId);

        log.info("레벨 진행도 계산 시작: userId={}, currentLevel={}, learnedWords={}, learnedSentences={}", 
                userId, currentLevel, learnedWords, learnedSentences);

        // 레벨별 필요 단어/문장 수 (level_settings 테이블에서 조회)
        LevelSettings settings = levelSettingsRepository.findByLevel(currentLevel).orElse(null);
        int wordsToNextLevel = (settings != null && settings.getWordsToNextLevel() > 0) ? settings.getWordsToNextLevel() : 100;
        int sentencesToNextLevel = (settings != null && settings.getSentencesToNextLevel() > 0) ? settings.getSentencesToNextLevel() : 50;

        log.info("레벨 설정: level={}, wordsToNextLevel={}, sentencesToNextLevel={}", 
                currentLevel, wordsToNextLevel, sentencesToNextLevel);

        // 누적 필요 단어/문장 수 (이전 레벨들의 요구사항 합계)
        int prevWordsTotal = getTotalWordsForPreviousLevels(currentLevel);
        int prevSentencesTotal = getTotalSentencesForPreviousLevels(currentLevel);

        log.info("이전 레벨 누적: prevWordsTotal={}, prevSentencesTotal={}", prevWordsTotal, prevSentencesTotal);

        // 현재 레벨에서 학습한 단어/문장 수
        int currentLevelLearnedWords = learnedWords - prevWordsTotal;
        int currentLevelLearnedSentences = learnedSentences - prevSentencesTotal;
        if (currentLevelLearnedWords < 0) currentLevelLearnedWords = 0;
        if (currentLevelLearnedSentences < 0) currentLevelLearnedSentences = 0;

        log.info("현재 레벨 학습량: currentLevelLearnedWords={}, currentLevelLearnedSentences={}", 
                currentLevelLearnedWords, currentLevelLearnedSentences);

        // 단어/문장 각각의 진행률 계산
        double wordProgress = wordsToNextLevel > 0 ? (currentLevelLearnedWords * 1.0) / wordsToNextLevel : 0;
        double sentenceProgress = sentencesToNextLevel > 0 ? (currentLevelLearnedSentences * 1.0) / sentencesToNextLevel : 0;

        log.info("개별 진행률: wordProgress={}, sentenceProgress={}", wordProgress, sentenceProgress);

        // 평균 진행률 (단어와 문장의 가중 평균)
        int levelProgress = (int) ((wordProgress + sentenceProgress) / 2 * 100);
        levelProgress = Math.min(levelProgress, 100);

        log.info("최종 레벨 진행도: {}%", levelProgress);

        LevelProgressDto dto = new LevelProgressDto();
        dto.setCurrentLevel(currentLevel);
        dto.setLevelProgress(levelProgress);
        dto.setWordsToNextLevel(Math.max(wordsToNextLevel - currentLevelLearnedWords, 0));
        dto.setSentencesToNextLevel(Math.max(sentencesToNextLevel - currentLevelLearnedSentences, 0));
        return dto;
    }

    /**
     * 기본 UserLevels 생성
     */
    private UserLevels createDefaultUserLevels(Long userId) {
        UserLevels userLevels = UserLevels.builder()
                .userId(userId)
                .currentLevel(1)
                .currentDay(1)
                .totalCoins(0)
                .experiencePoints(0)
                .streakDays(0)
                .build();
        return userLevelsRepository.save(userLevels);
    }

    private int getWordsToNextLevel(int currentLevel) {
        Optional<LevelSettings> opt = levelSettingsRepository.findByLevel(currentLevel);
        return opt.map(LevelSettings::getWordsToNextLevel).orElse(100);
    }

    private int getTotalWordsForPreviousLevels(int currentLevel) {
        List<LevelSettings> prevLevels = levelSettingsRepository.findByLevelLessThan(currentLevel);
        return prevLevels.stream().mapToInt(LevelSettings::getWordsToNextLevel).sum();
    }

    private int getTotalSentencesForPreviousLevels(int currentLevel) {
        List<LevelSettings> prevLevels = levelSettingsRepository.findByLevelLessThan(currentLevel);
        return prevLevels.stream().mapToInt(LevelSettings::getSentencesToNextLevel).sum();
    }

    @Transactional
    public void levelUpCurrentUser() {
        User user = getCurrentUser();
        levelUpUser(user.getId());
    }

    @Transactional
    public void levelUpUser(Long userId) {
        // UserLevels에서 현재 레벨 정보 조회
        UserLevels userLevels = userLevelsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultUserLevels(userId));
        
        int currentLevel = userLevels.getCurrentLevel();
        
        // 최대 레벨 확인 (기본값 10, 설정에서 가져올 수도 있음)
        int maxLevel = 10;
        
        if (currentLevel >= maxLevel) {
            throw new IllegalStateException("이미 최고 레벨에 도달했습니다.");
        }
        
        int nextLevel = currentLevel + 1;
        userLevels.setCurrentLevel(nextLevel);
        userLevels.setUpdatedAt(LocalDateTime.now());
        userLevelsRepository.save(userLevels);
        
        log.info("레벨업 완료: userId={}, oldLevel={}, newLevel={}", 
                userId, currentLevel, nextLevel);
    }
}