// ========== UserService.java (업데이트) ==========
package com.example.kidsreading.service;

import com.example.kidsreading.dto.RegisterRequest;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final EmailService emailService; // 나중에 추가

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
        return userRepository.findByUsername(username);
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
}