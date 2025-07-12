package com.example.kidsreading.service;

import com.example.kidsreading.entity.User;
import com.example.kidsreading.entity.UserWordProgress;
import com.example.kidsreading.entity.UserSentenceProgress;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final UserRepository userRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;

    /**
     * 학생 목록 조회 (검색 포함)
     */
    public List<Map<String, Object>> getStudents(String searchQuery) {
        try {
            List<User> students;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                // 검색어가 있는 경우 - 모든 학생을 가져온 후 필터링
                students = userRepository.findByRole(User.Role.STUDENT).stream()
                    .filter(student -> student.getName().contains(searchQuery) || 
                                     student.getEmail().contains(searchQuery))
                    .collect(Collectors.toList());
            } else {
                // 전체 학생 목록
                students = userRepository.findByRole(User.Role.STUDENT);
            }

            return students.stream()
                .map(student -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", student.getId());
                    studentMap.put("name", student.getName());
                    studentMap.put("email", student.getEmail());
                    studentMap.put("grade", getStudentGrade(student)); // 학년 계산 로직 필요
                    studentMap.put("isActive", student.getIsActive());
                    return studentMap;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("학생 목록 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 특정 학생의 기본 정보 조회
     */
    public Map<String, Object> getStudentInfo(Long studentId) {
        try {
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("id", student.getId());
            studentInfo.put("name", student.getName());
            studentInfo.put("email", student.getEmail());
            studentInfo.put("grade", getStudentGrade(student));
            studentInfo.put("isActive", student.getIsActive());
            studentInfo.put("createdAt", student.getCreatedAt());

            return studentInfo;
        } catch (Exception e) {
            log.error("학생 정보 조회 실패", e);
            throw e;
        }
    }

    /**
     * 학생의 학습 데이터 조회
     */
    public List<Map<String, Object>> getStudentLearningData(Long studentId, String startDate, String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            // 날짜별 학습 데이터 조회
            List<Map<String, Object>> learningData = start.datesUntil(end.plusDays(1))
                .map(date -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.toString());
                    
                    // 해당 날짜의 단어 학습 수
                    long wordsLearned = userWordProgressRepository.findByUserId(studentId).stream()
                        .filter(wp -> wp.getIsLearned() != null && wp.getIsLearned() && 
                                wp.getCreatedAt() != null && 
                                wp.getCreatedAt().toLocalDate().equals(date))
                        .count();
                    dayData.put("wordsLearned", wordsLearned);
                    
                    // 해당 날짜의 문장 학습 수
                    long sentencesLearned = userSentenceProgressRepository.findByUserId(studentId).stream()
                        .filter(sp -> sp.getIsCompleted() != null && sp.getIsCompleted() && 
                                sp.getCreatedAt() != null && 
                                sp.getCreatedAt().toLocalDate().equals(date))
                        .count();
                    dayData.put("sentencesLearned", sentencesLearned);
                    
                    // 해당 날짜의 코인 획득 (기본값)
                    dayData.put("coinsEarned", 0); // TODO: 코인 히스토리 구현 시 수정
                    
                    return dayData;
                })
                .collect(Collectors.toList());
            
            return learningData;
        } catch (Exception e) {
            log.error("학생 학습 데이터 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 학생에게 피드백 작성 (임시 구현)
     */
    public Map<String, Object> createFeedback(Long studentId, String content, Long teacherId) {
        try {
            // TODO: Feedback 엔티티와 Repository 구현 후 실제 저장 로직으로 변경
            Map<String, Object> feedbackMap = new HashMap<>();
            feedbackMap.put("id", System.currentTimeMillis()); // 임시 ID
            feedbackMap.put("content", content);
            feedbackMap.put("createdAt", LocalDateTime.now());
            feedbackMap.put("studentId", studentId);
            feedbackMap.put("teacherId", teacherId);
            
            log.info("피드백 작성: 학생ID={}, 교사ID={}, 내용={}", studentId, teacherId, content);
            
            return feedbackMap;
        } catch (Exception e) {
            log.error("피드백 작성 실패", e);
            throw e;
        }
    }

    /**
     * 피드백 수정 (임시 구현)
     */
    public Map<String, Object> updateFeedback(Long feedbackId, String content, Long teacherId) {
        try {
            // TODO: Feedback 엔티티와 Repository 구현 후 실제 수정 로직으로 변경
            Map<String, Object> feedbackMap = new HashMap<>();
            feedbackMap.put("id", feedbackId);
            feedbackMap.put("content", content);
            feedbackMap.put("updatedAt", LocalDateTime.now());
            
            log.info("피드백 수정: 피드백ID={}, 교사ID={}, 내용={}", feedbackId, teacherId, content);
            
            return feedbackMap;
        } catch (Exception e) {
            log.error("피드백 수정 실패", e);
            throw e;
        }
    }

    /**
     * 피드백 삭제 (임시 구현)
     */
    public void deleteFeedback(Long feedbackId, Long teacherId) {
        try {
            // TODO: Feedback 엔티티와 Repository 구현 후 실제 삭제 로직으로 변경
            log.info("피드백 삭제: 피드백ID={}, 교사ID={}", feedbackId, teacherId);
        } catch (Exception e) {
            log.error("피드백 삭제 실패", e);
            throw e;
        }
    }

    /**
     * 학생의 피드백 목록 조회 (임시 구현)
     */
    public List<Map<String, Object>> getStudentFeedback(Long studentId) {
        try {
            // TODO: Feedback 엔티티와 Repository 구현 후 실제 조회 로직으로 변경
            List<Map<String, Object>> feedbacks = List.of();
            
            log.info("학생 피드백 목록 조회: 학생ID={}", studentId);
            
            return feedbacks;
        } catch (Exception e) {
            log.error("학생 피드백 목록 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 학생 통계 요약 조회
     */
    public Map<String, Object> getStudentStats(Long studentId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 전체 학습한 단어 수
            long totalWordsLearned = userWordProgressRepository.findByUserId(studentId).stream()
                .filter(wp -> wp.getIsLearned() != null && wp.getIsLearned())
                .count();
            
            // 전체 학습한 문장 수
            long totalSentencesLearned = userSentenceProgressRepository.findByUserId(studentId).stream()
                .filter(sp -> sp.getIsCompleted() != null && sp.getIsCompleted())
                .count();
            
            // 오늘 학습한 단어 수
            long todayWordsLearned = userWordProgressRepository.findByUserId(studentId).stream()
                .filter(wp -> wp.getIsLearned() != null && wp.getIsLearned() && 
                        wp.getCreatedAt() != null && 
                        wp.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();
            
            // 오늘 학습한 문장 수
            long todaySentencesLearned = userSentenceProgressRepository.findByUserId(studentId).stream()
                .filter(sp -> sp.getIsCompleted() != null && sp.getIsCompleted() && 
                        sp.getCreatedAt() != null && 
                        sp.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();
            
            stats.put("totalWordsLearned", totalWordsLearned);
            stats.put("totalSentencesLearned", totalSentencesLearned);
            stats.put("todayWordsLearned", todayWordsLearned);
            stats.put("todaySentencesLearned", todaySentencesLearned);
            stats.put("totalCoins", 0); // TODO: 코인 히스토리 구현 시 수정
            
            return stats;
        } catch (Exception e) {
            log.error("학생 통계 조회 실패", e);
            return new HashMap<>();
        }
    }

    /**
     * 학생 학년 계산 (임시 로직)
     */
    private String getStudentGrade(User student) {
        // 실제로는 학생의 학년 정보가 별도 필드로 관리되어야 함
        // 현재는 임시로 기본값 반환
        return "3학년";
    }
} 