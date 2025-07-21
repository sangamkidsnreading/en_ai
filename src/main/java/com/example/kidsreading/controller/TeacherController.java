package com.example.kidsreading.controller;

import com.example.kidsreading.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.kidsreading.service.CustomUserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.example.kidsreading.dto.DayLevelProgressDto;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
import com.example.kidsreading.exception.AdminException;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.service.CustomUserDetailsService.CustomUserPrincipal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final TeacherService teacherService;
    private final UserRepository userRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;

    /**
     * 학생 목록 조회 (검색 포함, 권한 기반 필터링)
     */
    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getStudents(
            @RequestParam(required = false) String searchQuery,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).body(List.of());
            }

            // 사용자 역할 확인
            boolean isAdmin = "ADMIN".equals(user.getRole().name());
            String teacherGroupName = user.getGroupName();

            log.info("학생 목록 조회: 사용자ID={}, 역할={}, 그룹={}, 검색어={}", 
                    user.getId(), user.getRole(), teacherGroupName, searchQuery);

            List<Map<String, Object>> students = teacherService.getStudents(searchQuery, teacherGroupName, isAdmin);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("학생 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 학생의 기본 정보 조회
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentInfo(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            boolean isAdmin = "ADMIN".equals(user.getRole().name());
            String teacherGroupName = user.getGroupName();

            Map<String, Object> student = teacherService.getStudentInfo(studentId, teacherGroupName, isAdmin);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            log.error("학생 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 학생의 학습 데이터 조회 (달력용)
     */
    @GetMapping("/students/{studentId}/learning-data")
    public ResponseEntity<List<Map<String, Object>>> getStudentLearningData(
            @PathVariable Long studentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            boolean isAdmin = "ADMIN".equals(user.getRole().name());
            String teacherGroupName = user.getGroupName();

            List<Map<String, Object>> learningData = teacherService.getStudentLearningData(
                studentId, startDate, endDate, teacherGroupName, isAdmin);
            return ResponseEntity.ok(learningData);
        } catch (Exception e) {
            log.error("학생 학습 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 학생의 진행도 그래프 데이터 조회
    @GetMapping("/students/{studentId}/progress-graph")
    public ResponseEntity<Map<String, Object>> getStudentProgressGraph(@PathVariable Long studentId) {
        try {
            // 권한 확인
            CustomUserPrincipal currentUser = getCurrentUser();
            if (currentUser.getRole() == User.Role.TEACHER) {
                User teacher = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new AdminException("교사 정보를 찾을 수 없습니다."));
                
                User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new AdminException("학생 정보를 찾을 수 없습니다."));
                
                if (!teacher.getGroupName().equals(student.getGroupName())) {
                    throw new AdminException("다른 그룹의 학생 정보에 접근할 수 없습니다.");
                }
            }

            // 최근 7일간의 학습 데이터 조회
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);
            
            Map<String, Object> graphData = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<Long> wordsData = new ArrayList<>();
            List<Long> sentencesData = new ArrayList<>();
            
            // 각 날짜별 데이터 수집
            for (int i = 0; i < 7; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                labels.add(currentDate.format(DateTimeFormatter.ofPattern("M/d")));
                
                // 해당 날짜의 단어 학습 수
                long wordsCount = userWordProgressRepository.countLearnedWordsByUserIdBetweenDates(
                    studentId, currentDate, currentDate);
                wordsData.add(wordsCount);
                
                // 해당 날짜의 문장 학습 수
                long sentencesCount = userSentenceProgressRepository.countCompletedSentencesByUserIdBetweenDates(
                    studentId, currentDate, currentDate);
                sentencesData.add(sentencesCount);
            }
            
            graphData.put("labels", labels);
            graphData.put("wordsData", wordsData);
            graphData.put("sentencesData", sentencesData);
            
            return ResponseEntity.ok(graphData);
        } catch (Exception e) {
            log.error("학생 진행도 그래프 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 학생의 주별 진행도 그래프 데이터 조회
    @GetMapping("/students/{studentId}/weekly-progress")
    public ResponseEntity<Map<String, Object>> getStudentWeeklyProgress(@PathVariable Long studentId) {
        try {
            // 권한 확인
            CustomUserPrincipal currentUser = getCurrentUser();
            if (currentUser.getRole() == User.Role.TEACHER) {
                User teacher = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new AdminException("교사 정보를 찾을 수 없습니다."));
                
                User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new AdminException("학생 정보를 찾을 수 없습니다."));
                
                if (!teacher.getGroupName().equals(student.getGroupName())) {
                    throw new AdminException("다른 그룹의 학생 정보에 접근할 수 없습니다.");
                }
            }

            // 최근 4주간의 학습 데이터 조회
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusWeeks(3);
            
            Map<String, Object> graphData = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<Long> wordsData = new ArrayList<>();
            List<Long> sentencesData = new ArrayList<>();
            
            // 각 주별 데이터 수집
            for (int i = 0; i < 4; i++) {
                LocalDate weekStart = startDate.plusWeeks(i);
                LocalDate weekEnd = weekStart.plusDays(6);
                
                labels.add(weekStart.format(DateTimeFormatter.ofPattern("M/d")) + "~" + 
                          weekEnd.format(DateTimeFormatter.ofPattern("M/d")));
                
                // 해당 주의 단어 학습 수
                long wordsCount = userWordProgressRepository.countLearnedWordsByUserIdBetweenDates(
                    studentId, weekStart, weekEnd);
                wordsData.add(wordsCount);
                
                // 해당 주의 문장 학습 수
                long sentencesCount = userSentenceProgressRepository.countCompletedSentencesByUserIdBetweenDates(
                    studentId, weekStart, weekEnd);
                sentencesData.add(sentencesCount);
            }
            
            graphData.put("labels", labels);
            graphData.put("wordsData", wordsData);
            graphData.put("sentencesData", sentencesData);
            
            return ResponseEntity.ok(graphData);
        } catch (Exception e) {
            log.error("학생 주별 진행도 그래프 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private CustomUserPrincipal getCurrentUser() {
        return (CustomUserPrincipal) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 학생에게 피드백 작성
     */
    @PostMapping("/students/{studentId}/feedback")
    public ResponseEntity<Map<String, Object>> createFeedback(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            String content = request.get("content");
            Map<String, Object> feedback = teacherService.createFeedback(studentId, content, user.getId());
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            log.error("피드백 작성 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 피드백 수정
     */
    @PutMapping("/feedback/{feedbackId}")
    public ResponseEntity<Map<String, Object>> updateFeedback(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            String content = request.get("content");
            Map<String, Object> feedback = teacherService.updateFeedback(feedbackId, content, user.getId());
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            log.error("피드백 수정 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 피드백 삭제
     */
    @DeleteMapping("/feedback/{feedbackId}")
    public ResponseEntity<Map<String, String>> deleteFeedback(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            teacherService.deleteFeedback(feedbackId, user.getId());
            return ResponseEntity.ok(Map.of("message", "피드백이 삭제되었습니다."));
        } catch (Exception e) {
            log.error("피드백 삭제 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 학생의 피드백 목록 조회
     */
    @GetMapping("/students/{studentId}/feedback")
    public ResponseEntity<List<Map<String, Object>>> getStudentFeedback(@PathVariable Long studentId) {
        try {
            List<Map<String, Object>> feedbacks = teacherService.getStudentFeedback(studentId);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            log.error("학생 피드백 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 학생 통계 요약 조회
     */
    @GetMapping("/students/{studentId}/stats")
    public ResponseEntity<Map<String, Object>> getStudentStats(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            boolean isAdmin = "ADMIN".equals(user.getRole().name());
            String teacherGroupName = user.getGroupName();

            Map<String, Object> stats = teacherService.getStudentStats(studentId, teacherGroupName, isAdmin);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("학생 통계 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 학생의 Day/Level별 학습 진행도 조회
    @GetMapping("/students/{studentId}/day-level-progress")
    public ResponseEntity<List<DayLevelProgressDto>> getStudentDayLevelProgress(@PathVariable Long studentId) {
        try {
            List<DayLevelProgressDto> progress = teacherService.getStudentDayLevelProgress(studentId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            log.error("학생 Day/Level별 진행도 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 