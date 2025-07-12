// ========== AdminInitializer.java (수정) ==========
package com.example.kidsreading.config;

import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
            log.info("현재 데이터베이스의 사용자 수: {}", userRepository.count());

            System.out.println("=== 초기 관리자 계정 설정 시작 ===");
            System.out.println("기존 관리자 계정 확인 중...");

            Optional<User> existingAdmin = userRepository.findByEmail("admin@kidsreading.com");

            if (existingAdmin.isPresent()) {
                User admin = existingAdmin.get();
                System.out.println("기존 관리자 발견: ID=" + admin.getId() + ", Email=" + admin.getEmail());
            } else {
                System.out.println("기존 관리자가 없습니다. 새로 생성합니다.");

                User newAdmin = User.builder()
                        .username("admin")
                        .email("admin@kidsreading.com")
                        .password(passwordEncoder.encode("admin123!"))
                        .name("관리자")
                        .role(User.Role.ADMIN)
                        .isActive(true)
                        .emailVerified(true)
                        .build();

                User savedAdmin = userRepository.save(newAdmin);
                System.out.println("새 관리자 생성 완료: ID=" + savedAdmin.getId());
            }

            log.info("초기화 완료 후 사용자 수: {}", userRepository.count());

            // 모든 사용자 출력 (디버깅용) - 안전한 방식으로 처리
            try {
                List<User> allUsers = userRepository.findAll();
                log.info("=== 전체 사용자 목록 ===");
                for (User user : allUsers) {
                    log.info("사용자: ID={}, 이메일={}, 역할={}, 활성={}", 
                        user.getId(), user.getEmail(), user.getRole(), user.getIsActive());
                }
            } catch (Exception userListException) {
                log.error("사용자 목록 조회 중 오류 발생 (enum 불일치 가능성): {}", userListException.getMessage());
                log.info("사용자 목록 조회를 건너뜁니다.");
            }

        } catch (Exception e) {
            log.error("AdminInitializer 실행 중 오류 발생", e);
            log.info("애플리케이션은 계속 실행됩니다.");
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