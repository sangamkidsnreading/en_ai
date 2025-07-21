package com.example.kidsreading.service;

import com.example.kidsreading.entity.UserAttendance;
import com.example.kidsreading.repository.UserAttendanceRepository;
import com.example.kidsreading.util.DeviceDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAttendanceService {
    
    private final UserAttendanceRepository userAttendanceRepository;
    private final DeviceDetector deviceDetector;
    
    /**
     * 사용자 출석체크
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param ipAddress IP 주소
     * @param userAgent User-Agent 문자열
     * @return 출석체크 결과 (새로 출석한 경우 true, 이미 출석한 경우 false)
     */
    @Transactional
    public boolean checkAttendance(Long userId, String email, String ipAddress, String userAgent) {
        LocalDate today = LocalDate.now();
        
        // 오늘 이미 출석했는지 확인
        Optional<UserAttendance> existingAttendance = userAttendanceRepository.findByUserIdAndAttendanceDate(userId, today);
        
        if (existingAttendance.isPresent()) {
            log.info("사용자 {} (ID: {})는 오늘 이미 출석했습니다.", email, userId);
            return false;
        }
        
        // 디바이스 타입 감지
        String deviceType = deviceDetector.detectDeviceType(userAgent);
        
        // 새로운 출석 기록 생성
        UserAttendance attendance = UserAttendance.builder()
                .userId(userId)
                .email(email)
                .attendanceDate(today)
                .ipAddress(ipAddress)
                .deviceType(deviceType)
                .userAgent(userAgent)
                .build();
        
        userAttendanceRepository.save(attendance);
        
        log.info("사용자 {} (ID: {}) 출석체크 완료 - 디바이스: {}, IP: {}", 
                email, userId, deviceType, ipAddress);
        
        return true;
    }
    
    /**
     * 사용자의 특정 날짜 출석 여부 확인
     */
    public boolean hasAttendedOnDate(Long userId, LocalDate date) {
        return userAttendanceRepository.findByUserIdAndAttendanceDate(userId, date).isPresent();
    }
    
    /**
     * 사용자의 오늘 출석 여부 확인
     */
    public boolean hasAttendedToday(Long userId) {
        return hasAttendedOnDate(userId, LocalDate.now());
    }
    
    /**
     * 사용자의 모든 출석 기록 조회
     */
    public List<UserAttendance> getUserAttendanceHistory(Long userId) {
        return userAttendanceRepository.findByUserIdOrderByAttendanceDateDesc(userId);
    }
    
    /**
     * 사용자의 특정 기간 출석 기록 조회
     */
    public List<UserAttendance> getUserAttendanceByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return userAttendanceRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * 사용자의 총 출석 일수 조회
     */
    public Long getTotalAttendanceDays(Long userId) {
        return userAttendanceRepository.countByUserId(userId);
    }
    
    /**
     * 사용자의 연속 출석 일수 계산
     */
    public int getConsecutiveAttendanceDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;
        int consecutiveDays = 0;
        
        while (hasAttendedOnDate(userId, checkDate)) {
            consecutiveDays++;
            checkDate = checkDate.minusDays(1);
        }
        
        return consecutiveDays;
    }
    
    /**
     * 사용자의 마지막 출석 날짜 조회
     */
    public Optional<LocalDate> getLastAttendanceDate(Long userId) {
        return userAttendanceRepository.findLastAttendanceDateByUserId(userId);
    }
    
    /**
     * 특정 날짜의 모든 출석 기록 조회
     */
    public List<UserAttendance> getAttendanceByDate(LocalDate date) {
        return userAttendanceRepository.findByAttendanceDateOrderByCreatedAtDesc(date);
    }
} 