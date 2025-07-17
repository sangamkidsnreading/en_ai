// ========== UserService.java (업데이트) ==========
package com.example.kidsreading.service;

import com.example.kidsreading.dto.LevelProgressDto;
import com.example.kidsreading.dto.RegisterRequest;
import com.example.kidsreading.entity.LevelSettings;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.LevelSettingsRepository;
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
    // private final EmailService emailService; // 나중에 추가

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    /**
     * 회원가입 처리
     */
    public User registerUser(RegisterRequest request) {
        log.info("회원가입 처리 시작: {}", request.getEmail());

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
        int learnedWords = userWordProgressRepository.countByUserIdAndIsLearnedTrue(user.getId());
        int learnedSentences = userSentenceProgressRepository.countByUserIdAndIsLearnedTrue(user.getId());
        int currentLevel = user.getLevel() != null ? user.getLevel() : 1;

        // 레벨별 필요 단어/문장 수
        LevelSettings settings = levelSettingsRepository.findByLevel(currentLevel).orElse(null);
        int wordsToNextLevel = (settings != null && settings.getWordsToNextLevel() > 0) ? settings.getWordsToNextLevel() : 100;
        int sentencesToNextLevel = (settings != null && settings.getSentencesToNextLevel() > 0) ? settings.getSentencesToNextLevel() : 50;

        // 누적 필요 단어/문장 수
        int prevWordsTotal = getTotalWordsForPreviousLevels(currentLevel);
        int prevSentencesTotal = getTotalSentencesForPreviousLevels(currentLevel);

        int currentLevelLearnedWords = learnedWords - prevWordsTotal;
        int currentLevelLearnedSentences = learnedSentences - prevSentencesTotal;
        if (currentLevelLearnedWords < 0) currentLevelLearnedWords = 0;
        if (currentLevelLearnedSentences < 0) currentLevelLearnedSentences = 0;

        // 단어/문장 각각의 진행률
        double wordProgress = (currentLevelLearnedWords * 1.0) / wordsToNextLevel;
        double sentenceProgress = (currentLevelLearnedSentences * 1.0) / sentencesToNextLevel;

        // 평균 진행률(혹은 가중치 부여 가능)
        int levelProgress = (int) ( (wordProgress + sentenceProgress) / 2 * 100 );
        levelProgress = Math.min(levelProgress, 100);

        LevelProgressDto dto = new LevelProgressDto();
        dto.setCurrentLevel(currentLevel);
        dto.setLevelProgress(levelProgress);
        dto.setWordsToNextLevel(Math.max(wordsToNextLevel - currentLevelLearnedWords, 0));
        dto.setSentencesToNextLevel(Math.max(sentencesToNextLevel - currentLevelLearnedSentences, 0));
        return dto;
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
        int currentLevel = user.getLevel() != null ? user.getLevel() : 1;
        int nextLevel = currentLevel + 1;
        user.setLevel(nextLevel);
        userRepository.save(user);
    }
}