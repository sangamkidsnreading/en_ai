package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAttendanceRepository extends JpaRepository<UserAttendance, Long> {
    
    // 특정 사용자의 특정 날짜 출석 기록 조회
    Optional<UserAttendance> findByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);
    
    // 특정 사용자의 모든 출석 기록 조회
    List<UserAttendance> findByUserIdOrderByAttendanceDateDesc(Long userId);
    
    // 특정 사용자의 특정 기간 출석 기록 조회
    @Query("SELECT ua FROM UserAttendance ua WHERE ua.userId = :userId AND ua.attendanceDate BETWEEN :startDate AND :endDate ORDER BY ua.attendanceDate DESC")
    List<UserAttendance> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // 특정 날짜의 모든 출석 기록 조회
    List<UserAttendance> findByAttendanceDateOrderByCreatedAtDesc(LocalDate attendanceDate);
    
    // 특정 사용자의 연속 출석 일수 조회
    @Query("SELECT COUNT(ua) FROM UserAttendance ua WHERE ua.userId = :userId AND ua.attendanceDate >= :startDate")
    Long countConsecutiveAttendance(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    // 특정 사용자의 총 출석 일수 조회
    Long countByUserId(Long userId);
    
    // 특정 사용자의 마지막 출석 날짜 조회
    @Query("SELECT MAX(ua.attendanceDate) FROM UserAttendance ua WHERE ua.userId = :userId")
    Optional<LocalDate> findLastAttendanceDateByUserId(@Param("userId") Long userId);
} 