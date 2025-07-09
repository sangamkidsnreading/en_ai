// ========== UserRepository.java (간단 버전 - 에러 해결) ==========
package com.example.kidsreading.repository;

import com.example.kidsreading.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== 기본 조회 메서드 ==========
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByUsernameContainingOrEmailContaining(String username, String email);
    long countByRole(User.Role role);

    // ========== 회원가입 관련 메서드 ==========

    /**
     * 연락처 중복 확인
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 연락처로 사용자 조회
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * 이메일과 인증번호로 사용자 조회
     */
    Optional<User> findByEmailAndVerificationCode(String email, String verificationCode);

    /**
     * 활성화되지 않은 사용자 중 이메일로 조회 (임시 사용자)
     */
    Optional<User> findByEmailAndIsActiveFalse(String email);

    /**
     * 이메일 인증이 완료되지 않은 사용자 조회
     */
    Optional<User> findByEmailAndEmailVerifiedFalse(String email);

    /**
     * 사용자명과 이메일 동시 조회 (비밀번호 재설정용)
     */
    Optional<User> findByUsernameAndEmail(String username, String email);

    /**
     * 만료된 인증번호를 가진 사용자들 조회 (정리용)
     */
    @Query("SELECT u FROM User u WHERE u.verificationCodeExpiresAt < :now AND u.emailVerified = false")
    List<User> findExpiredVerificationUsers(@Param("now") LocalDateTime now);

    /**
     * 특정 시간 이후 생성된 미인증 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :since AND u.emailVerified = false AND u.isActive = false")
    List<User> findUnverifiedUsersSince(@Param("since") LocalDateTime since);

    /**
     * 인증번호가 만료되지 않은 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.verificationCode = :code AND u.verificationCodeExpiresAt > :now")
    Optional<User> findByEmailAndValidVerificationCode(
            @Param("email") String email,
            @Param("code") String verificationCode,
            @Param("now") LocalDateTime now);

    /**
     * 부모 이름으로 사용자 조회 (가족 연결용)
     */
    List<User> findByParentName(String parentName);

    /**
     * 부모 이름과 연락처로 사용자 조회
     */
    Optional<User> findByParentNameAndPhoneNumber(String parentName, String phoneNumber);

    // ========== 그룹 및 역할 관련 ==========

    /**
     * 특정 그룹에 속한 사용자들 조회
     */
    List<User> findByGroupName(String groupName);

    /**
     * 특정 그룹의 활성 사용자들 조회
     */
    List<User> findByGroupNameAndIsActiveTrue(String groupName);

    /**
     * 역할별 사용자 조회
     */
    List<User> findByRole(User.Role role);

    /**
     * 역할별 활성 사용자 조회
     */
    List<User> findByRoleAndIsActiveTrue(User.Role role);

    /**
     * 학부모 역할의 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = 'PARENT' AND u.isActive = true")
    List<User> findAllActiveParents();

    /**
     * 학생 역할의 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.isActive = true")
    List<User> findAllActiveStudents();

    /**
     * 관리자 역할의 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

    // ========== 인증 및 활성화 상태 관련 ==========

    /**
     * 이메일 인증 완료된 사용자들 조회
     */
    List<User> findByEmailVerifiedTrue();

    /**
     * 이메일 인증 미완료된 사용자들 조회
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * 활성 사용자들 조회
     */
    List<User> findByIsActiveTrue();

    /**
     * 비활성 사용자들 조회
     */
    List<User> findByIsActiveFalse();

    /**
     * 이메일 인증 완료이고 활성화된 사용자들
     */
    List<User> findByEmailVerifiedTrueAndIsActiveTrue();

    // ========== 검색 및 필터링 ==========

    /**
     * 이름, 이메일, 사용자명으로 검색
     */
    List<User> findByUsernameContainingOrEmailContainingOrNameContaining(
            String username, String email, String name);

    /**
     * 부모 이름으로 검색
     */
    List<User> findByParentNameContaining(String parentName);

    /**
     * 연락처로 검색 (부분 일치)
     */
    List<User> findByPhoneNumberContaining(String phoneNumber);

    /**
     * 복합 검색 (이름, 이메일, 부모이름, 연락처)
     */
    @Query("SELECT u FROM User u WHERE " +
            "u.name LIKE %:keyword% OR " +
            "u.email LIKE %:keyword% OR " +
            "u.parentName LIKE %:keyword% OR " +
            "u.phoneNumber LIKE %:keyword% OR " +
            "u.username LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);

    // ========== 날짜 관련 조회 ==========

    /**
     * 특정 날짜 이후 가입한 사용자들
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * 특정 기간 내 가입한 사용자들
     */
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 날짜 이후 마지막 로그인한 사용자들
     */
    List<User> findByLastLoginAfter(LocalDateTime date);

    /**
     * 특정 기간 내 마지막 로그인한 사용자들
     */
    List<User> findByLastLoginBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 생년월일 범위의 사용자들
     */
    List<User> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 나이대의 사용자들 (생년월일 기준)
     */
    @Query("SELECT u FROM User u WHERE YEAR(CURRENT_DATE) - YEAR(u.birthDate) BETWEEN :minAge AND :maxAge")
    List<User> findByAgeBetween(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

    // ========== 통계 및 집계 ==========

    /**
     * 역할별 사용자 수 조회
     */
    //long countByRole(User.Role role);  // Moved to the top

    /**
     * 그룹별 사용자 수 조회
     */
    long countByGroupName(String groupName);

    /**
     * 특정 기간 내 가입한 사용자 수
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 기간 내 로그인한 사용자 수
     */
    long countByLastLoginBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 이메일 인증 완료 사용자 수
     */
    long countByEmailVerifiedTrue();

    /**
     * 활성 사용자 수
     */
    long countByIsActiveTrue();

    /**
     * 그룹별 사용자 수 집계
     */
    @Query("SELECT u.groupName, COUNT(u) FROM User u GROUP BY u.groupName")
    List<Object[]> countUsersByGroup();

    /**
     * 역할별 사용자 수 집계
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * 월별 가입자 수 집계 (최근 12개월)
     */
    @Query("SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) " +
            "FROM User u WHERE u.createdAt >= :since " +
            "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) " +
            "ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)")
    List<Object[]> getMonthlyRegistrationStats(@Param("since") LocalDateTime since);

    /**
     * 사용자명 또는 이메일로 사용자 조회
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    // ========== 사용자 관리용 ==========

    /**
     * 장기간 미로그인 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :cutoffDate OR u.lastLogin IS NULL")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 인증 코드 만료 임박 사용자들
     */
    @Query("SELECT u FROM User u WHERE u.verificationCodeExpiresAt BETWEEN :now AND :soonExpiry AND u.emailVerified = false")
    List<User> findUsersWithSoonExpiringCodes(
            @Param("now") LocalDateTime now,
            @Param("soonExpiry") LocalDateTime soonExpiry);

    /**
     * 가입 후 일정 시간 내 미인증 사용자들 (삭제 대상)
     */
    @Query("SELECT u FROM User u WHERE u.createdAt < :cutoffDate AND u.emailVerified = false AND u.isActive = false")
    List<User> findUnverifiedUsersForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 그룹의 관리자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND (u.groupName = :groupName OR u.groupName IS NULL)")
    List<User> findAdminsByGroup(@Param("groupName") String groupName);

    /**
     * 중복 이메일 체크 (대소문자 무시)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    List<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * 중복 사용자명 체크 (대소문자 무시)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    List<User> findByUsernameIgnoreCase(@Param("username") String username);

    // ========== 고급 검색 ==========

    /**
     * 다중 조건 검색
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:groupName IS NULL OR u.groupName = :groupName) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive) AND " +
            "(:emailVerified IS NULL OR u.emailVerified = :emailVerified)")
    List<User> findUsersWithFilters(
            @Param("role") User.Role role,
            @Param("groupName") String groupName,
            @Param("isActive") Boolean isActive,
            @Param("emailVerified") Boolean emailVerified);

    long countByLastLoginAfter(LocalDateTime localDateTime);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String query, String query1, String query2);

    // ========== 학습 활동 관련 (간단 버전) ==========
    // 아래 메서드들은 UserWordProgress, UserSentenceProgress 엔티티가 생성된 후에 활성화하세요

    /*
    /**
     * 학습 활동이 있는 사용자들 조회
     */
    /*
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN u.wordProgresses uwp " +
           "LEFT JOIN u.sentenceProgresses usp " +
           "WHERE uwp.id IS NOT NULL OR usp.id IS NOT NULL")
    List<User> findUsersWithLearningActivity();
    */

    /*
    /**
     * 최근 활동한 사용자들 조회
     */
    /*
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN u.wordProgresses uwp " +
           "LEFT JOIN u.sentenceProgresses usp " +
           "WHERE u.lastLogin >= :since OR uwp.lastPracticedAt >= :since OR usp.lastPracticedAt >= :since")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);
    */
}