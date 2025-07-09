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
import java.util.Optional;

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
            initializeAdminAndTestUser();

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

    private void initializeAdminAndTestUser() {
        System.out.println("=== 초기 관리자 계정 설정 시작 ===");

        String adminEmail = "admin@kidsreading.com";

        try {
            System.out.println("기존 관리자 계정 확인 중...");

            Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
            if (existingAdmin.isPresent()) {
                User user = existingAdmin.get();
                System.out.println("기존 관리자 발견: ID=" + user.getId() + ", Email=" + user.getEmail());
                return;
            }

            System.out.println("기존 관리자가 없습니다. 새로 생성합니다.");
        } catch (Exception e) {
            System.err.println("기존 사용자 확인 중 오류: " + adminEmail);
            e.printStackTrace();
        }

        // 강제로 관리자 생성
        try {
            System.out.println("관리자 계정 생성 시작: " + adminEmail);

            User admin = new User();
            admin.setUsername(adminEmail);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123!"));
            admin.setName("관리자");
            admin.setParentName("관리자");
            admin.setPhoneNumber("010-0000-0000");
            admin.setGroupName("admin");
            admin.setRole(User.Role.ADMIN);
            admin.setIsActive(true);
            admin.setEmailVerified(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            User savedAdmin = userRepository.save(admin);
            userRepository.flush(); // 즉시 DB에 반영

            System.out.println("✅ 관리자 계정이 생성되었습니다: " + savedAdmin.getEmail() + " (ID: " + savedAdmin.getId() + ")");
        } catch (Exception e) {
            System.err.println("❌ 관리자 계정 생성 실패: " + adminEmail);
            e.printStackTrace();
        }

        // 테스트 사용자도 생성
        try {
            System.out.println("테스트 사용자 생성 시작");

            User testUser = new User();
            testUser.setUsername("test@kidsreading.com");
            testUser.setEmail("test@kidsreading.com");
            testUser.setPassword(passwordEncoder.encode("test123!"));
            testUser.setName("테스트 사용자");
            testUser.setParentName("테스트 부모");
            testUser.setPhoneNumber("010-1234-5678");
            testUser.setGroupName("test");
            testUser.setRole(User.Role.USER);
            testUser.setIsActive(true);
            testUser.setEmailVerified(true);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(testUser);
            userRepository.flush();

            System.out.println("✅ 테스트 사용자가 생성되었습니다: " + savedUser.getEmail() + " (ID: " + savedUser.getId() + ")");
        } catch (Exception e) {
            System.err.println("❌ 테스트 사용자 생성 실패");
            e.printStackTrace();
        }

        System.out.println("=== 초기 계정 설정 완료 ===");
    }
}