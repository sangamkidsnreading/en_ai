// ========== DebugController.java (데이터베이스 상태 확인용) ==========
package com.example.kidsreading.controller;

import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @GetMapping("/debug/check-db")
    @ResponseBody
    public String checkDatabase() {
        StringBuilder result = new StringBuilder();

        try {
            // 1. JPA Repository를 통한 확인
            long jpaCount = userRepository.count();
            result.append("=== JPA Repository 확인 ===\n");
            result.append("사용자 수: ").append(jpaCount).append("\n\n");

            if (jpaCount > 0) {
                result.append("=== JPA 사용자 목록 ===\n");
                userRepository.findAll().forEach(user -> {
                    result.append(String.format("ID: %d, Email: %s, Username: %s, Role: %s\n",
                            user.getId(), user.getEmail(), user.getUsername(), user.getRole()));
                });
                result.append("\n");
            }

            // 2. 직접 SQL을 통한 확인
            result.append("=== 직접 SQL 확인 ===\n");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 테이블 존재 확인
                ResultSet tables = stmt.executeQuery("SHOW TABLES");
                result.append("존재하는 테이블:\n");
                while (tables.next()) {
                    result.append("- ").append(tables.getString(1)).append("\n");
                }
                result.append("\n");

                // USERS 테이블 직접 조회
                try {
                    ResultSet users = stmt.executeQuery("SELECT COUNT(*) FROM USERS");
                    if (users.next()) {
                        result.append("USERS 테이블 레코드 수: ").append(users.getInt(1)).append("\n");
                    }

                    ResultSet userList = stmt.executeQuery("SELECT id, email, username, role FROM USERS LIMIT 10");
                    result.append("USERS 테이블 데이터:\n");
                    while (userList.next()) {
                        result.append(String.format("ID: %d, Email: %s, Username: %s, Role: %s\n",
                                userList.getLong("id"),
                                userList.getString("email"),
                                userList.getString("username"),
                                userList.getString("role")));
                    }
                } catch (Exception e) {
                    result.append("USERS 테이블 조회 실패: ").append(e.getMessage()).append("\n");
                }
            }

        } catch (Exception e) {
            result.append("데이터베이스 확인 중 오류: ").append(e.getMessage()).append("\n");
            log.error("데이터베이스 확인 중 오류 발생", e);
        }

        return result.toString();
    }

    @GetMapping("/debug/force-create-users")
    @ResponseBody
    public String forceCreateUsers() {
        StringBuilder result = new StringBuilder();

        try {
            result.append("=== 강제 사용자 생성 시작 ===\n");

            // 현재 상태 확인
            long beforeCount = userRepository.count();
            result.append("생성 전 사용자 수: ").append(beforeCount).append("\n");

            // 관리자 생성
            String adminEmail = "admin@kidsreading.com";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
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

                User savedAdmin = userRepository.saveAndFlush(admin);
                result.append("✅ 관리자 생성: ").append(savedAdmin.getEmail()).append(" (ID: ").append(savedAdmin.getId()).append(")\n");
            } else {
                result.append("⚠️ 관리자 이미 존재\n");
            }

            // 테스트 사용자 생성
            String testEmail = "happymega13579@gmail.com";
            if (userRepository.findByEmail(testEmail).isEmpty()) {
                User testUser = new User();
                testUser.setUsername(testEmail);
                testUser.setEmail(testEmail);
                testUser.setPassword(passwordEncoder.encode("test123!"));
                testUser.setName("테스트 사용자");
                testUser.setParentName("테스트 부모");
                testUser.setPhoneNumber("010-1234-5678");
                testUser.setGroupName("group1");
                testUser.setRole(User.Role.PARENT);
                testUser.setIsActive(true);
                testUser.setEmailVerified(true);
                testUser.setCreatedAt(LocalDateTime.now());
                testUser.setUpdatedAt(LocalDateTime.now());

                User savedUser = userRepository.saveAndFlush(testUser);
                result.append("✅ 테스트 사용자 생성: ").append(savedUser.getEmail()).append(" (ID: ").append(savedUser.getId()).append(")\n");
            } else {
                result.append("⚠️ 테스트 사용자 이미 존재\n");
            }

            // 최종 확인
            long afterCount = userRepository.count();
            result.append("생성 후 사용자 수: ").append(afterCount).append("\n");

            result.append("\n=== 생성된 사용자 목록 ===\n");
            userRepository.findAll().forEach(user -> {
                result.append(String.format("ID: %d, Email: %s, Role: %s, Active: %s\n",
                        user.getId(), user.getEmail(), user.getRole(), user.getIsActive()));
            });

        } catch (Exception e) {
            result.append("❌ 사용자 생성 중 오류: ").append(e.getMessage()).append("\n");
            log.error("사용자 생성 중 오류 발생", e);
        }

        return result.toString();
    }

    @GetMapping("/debug/test-login")
    @ResponseBody
    public String testLogin() {
        StringBuilder result = new StringBuilder();

        try {
            String testEmail = "happymega13579@gmail.com";

            // 이메일로 조회
            var userByEmail = userRepository.findByEmail(testEmail);
            result.append("이메일로 조회 결과: ").append(userByEmail.isPresent() ? "발견됨" : "없음").append("\n");

            if (userByEmail.isPresent()) {
                User user = userByEmail.get();
                result.append("사용자 정보:\n");
                result.append("- ID: ").append(user.getId()).append("\n");
                result.append("- Username: ").append(user.getUsername()).append("\n");
                result.append("- Email: ").append(user.getEmail()).append("\n");
                result.append("- Role: ").append(user.getRole()).append("\n");
                result.append("- Active: ").append(user.getIsActive()).append("\n");
                result.append("- EmailVerified: ").append(user.getEmailVerified()).append("\n");
                result.append("- Password (암호화됨): ").append(user.getPassword()).append("\n");
            }

            // 사용자명으로도 조회
            var userByUsername = userRepository.findByUsername(testEmail);
            result.append("\n사용자명으로 조회 결과: ").append(userByUsername.isPresent() ? "발견됨" : "없음").append("\n");

        } catch (Exception e) {
            result.append("❌ 테스트 로그인 확인 중 오류: ").append(e.getMessage()).append("\n");
            log.error("테스트 로그인 확인 중 오류 발생", e);
        }

        return result.toString();
    }
}