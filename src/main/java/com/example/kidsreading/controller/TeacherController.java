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

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * 학생 목록 조회 (검색 포함)
     */
    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getStudents(
            @RequestParam(required = false) String searchQuery,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        try {
            List<Map<String, Object>> students = teacherService.getStudents(searchQuery);
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
    public ResponseEntity<Map<String, Object>> getStudentInfo(@PathVariable Long studentId) {
        try {
            Map<String, Object> student = teacherService.getStudentInfo(studentId);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            log.error("학생 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 학생의 학습 데이터 조회
     */
    @GetMapping("/students/{studentId}/learning-data")
    public ResponseEntity<List<Map<String, Object>>> getStudentLearningData(
            @PathVariable Long studentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> learningData = teacherService.getStudentLearningData(studentId, startDate, endDate);
            return ResponseEntity.ok(learningData);
        } catch (Exception e) {
            log.error("학생 학습 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
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
    public ResponseEntity<Map<String, Object>> getStudentStats(@PathVariable Long studentId) {
        try {
            Map<String, Object> stats = teacherService.getStudentStats(studentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("학생 통계 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 