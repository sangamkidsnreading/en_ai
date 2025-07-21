package com.example.kidsreading.controller;

import com.example.kidsreading.entity.UserAttendance;
import com.example.kidsreading.service.UserAttendanceService;
import com.example.kidsreading.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
public class UserAttendanceController {
    
    private final UserAttendanceService userAttendanceService;
    private final UserService userService;
    
    /**
     * 사용자의 출석 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAttendanceStats(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = authentication.getName();
            Optional<com.example.kidsreading.entity.User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            com.example.kidsreading.entity.User user = userOpt.get();
            Long userId = user.getId();
            
            // 출석 통계 계산
            Long totalAttendanceDays = userAttendanceService.getTotalAttendanceDays(userId);
            int consecutiveAttendanceDays = userAttendanceService.getConsecutiveAttendanceDays(userId);
            boolean hasAttendedToday = userAttendanceService.hasAttendedToday(userId);
            Optional<LocalDate> lastAttendanceDate = userAttendanceService.getLastAttendanceDate(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAttendanceDays", totalAttendanceDays);
            stats.put("consecutiveAttendanceDays", consecutiveAttendanceDays);
            stats.put("hasAttendedToday", hasAttendedToday);
            stats.put("lastAttendanceDate", lastAttendanceDate.orElse(null));
            
            response.put("success", true);
            response.put("stats", stats);
            
        } catch (Exception e) {
            log.error("출석 통계 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "출석 통계 조회 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자의 출석 기록 조회
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getAttendanceHistory(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = authentication.getName();
            Optional<com.example.kidsreading.entity.User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            com.example.kidsreading.entity.User user = userOpt.get();
            Long userId = user.getId();
            
            List<UserAttendance> attendanceHistory;
            
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                attendanceHistory = userAttendanceService.getUserAttendanceByDateRange(userId, start, end);
            } else {
                attendanceHistory = userAttendanceService.getUserAttendanceHistory(userId);
            }
            
            response.put("success", true);
            response.put("attendanceHistory", attendanceHistory);
            
        } catch (Exception e) {
            log.error("출석 기록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "출석 기록 조회 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 오늘의 출석 여부 확인
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> checkTodayAttendance(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = authentication.getName();
            Optional<com.example.kidsreading.entity.User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            com.example.kidsreading.entity.User user = userOpt.get();
            boolean hasAttendedToday = userAttendanceService.hasAttendedToday(user.getId());
            
            response.put("success", true);
            response.put("hasAttendedToday", hasAttendedToday);
            
        } catch (Exception e) {
            log.error("오늘 출석 확인 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "출석 확인 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 관리자용: 특정 날짜의 모든 출석 기록 조회
     */
    @GetMapping("/admin/date/{date}")
    public ResponseEntity<Map<String, Object>> getAttendanceByDate(@PathVariable String date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate attendanceDate = LocalDate.parse(date);
            List<UserAttendance> attendanceList = userAttendanceService.getAttendanceByDate(attendanceDate);
            
            response.put("success", true);
            response.put("attendanceList", attendanceList);
            response.put("totalCount", attendanceList.size());
            
        } catch (Exception e) {
            log.error("날짜별 출석 기록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "출석 기록 조회 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
} 