// ========== AdminInitializer.java (수정) ==========
package com.example.kidsreading.config;

import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // 다른 Bean들보다 먼저 실행
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== AdminInitializer 시작 ===");

        try {
            // 현재 사용자 수 확인
            long currentUserCount = userRepository.count();
            log.info("현재 데이터베이스의 사용자 수: {}", currentUserCount);

            // 테스트 데이터 생성
            createDefaultAdmin();
            createTestUser();

            // 최종 사용자 수 확인
            long finalUserCount = userRepository.count();
            log.info("초기화 완료 후 사용자 수: {}", finalUserCount);

            // 모든 사용자 출력
            userRepository.findAll().forEach(user -> {
                log.info("사용자: ID={}, Email={}, Username={}, Role={}, Active={}",
                        user.getId(), user.getEmail(), user.getUsername(), user.getRole(), user.getIsActive());
            });

        } catch (Exception e) {
            log.error("AdminInitializer 실행 중 오류 발생", e);
            throw e;
        }

        log.info("=== AdminInitializer 완료 ===");
    }

    private void createDefaultAdmin() {
        String adminEmail = "admin@kidsreading.com";

        try {
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                log.info("관리자 계정 생성 시작: {}", adminEmail);

                User admin = User.builder()
                        .username(adminEmail)
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123!"))
                        .name("관리자")
                        .parentName("관리자")
                        .phoneNumber("010-0000-0000")
                        .groupName("admin")
                        .role(User.Role.ADMIN)
                        .isActive(true)
                        .emailVerified(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                User savedAdmin = userRepository.save(admin);
                userRepository.flush(); // 즉시 DB에 반영

                log.info("✅ 관리자 계정이 생성되었습니다: {} (ID: {})", savedAdmin.getEmail(), savedAdmin.getId());
            } else {
                log.info("⚠️ 관리자 계정이 이미 존재합니다: {}", adminEmail);
            }
        } catch (Exception e) {
            log.error("❌ 관리자 계정 생성 실패: {}", adminEmail, e);
            throw e;
        }
    }

    private void createTestUser() {
        String testEmail = "happymega13579@gmail.com";

        try {
            if (userRepository.findByEmail(testEmail).isEmpty()) {
                log.info("테스트 계정 생성 시작: {}", testEmail);

                User testUser = User.builder()
                        .username(testEmail)
                        .email(testEmail)
                        .password(passwordEncoder.encode("test123!"))
                        .name("테스트 사용자")
                        .parentName("테스트 부모")
                        .phoneNumber("010-1234-5678")
                        .groupName("group1")
                        .role(User.Role.PARENT)
                        .isActive(true)
                        .emailVerified(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                User savedUser = userRepository.save(testUser);
                userRepository.flush(); // 즉시 DB에 반영

                log.info("✅ 테스트 계정이 생성되었습니다: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
            } else {
                log.info("⚠️ 테스트 계정이 이미 존재합니다: {}", testEmail);
            }
        } catch (Exception e) {
            log.error("❌ 테스트 계정 생성 실패: {}", testEmail, e);
            throw e;
        }
    }
}