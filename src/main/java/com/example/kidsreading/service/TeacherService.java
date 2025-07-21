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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.example.kidsreading.dto.DayLevelProgressDto;
import com.example.kidsreading.exception.AdminException;
import com.example.kidsreading.service.CustomUserDetailsService.CustomUserPrincipal;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final UserRepository userRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;

    /**
     * 학생 목록 조회 (검색 포함, group_name 기반 필터링)
     */
    public List<Map<String, Object>> getStudents(String searchQuery, String teacherGroupName, boolean isAdmin) {
        try {
            List<User> students;
            
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                // 검색어가 있는 경우
                if (isAdmin) {
                    // ADMIN은 모든 학생 검색 가능
                    students = userRepository.findByRole(User.Role.STUDENT).stream()
                        .filter(student -> student.getName().contains(searchQuery) || 
                                         student.getEmail().contains(searchQuery))
                        .collect(Collectors.toList());
                } else {
                    // TEACHER는 자신의 group_name에 해당하는 학생만 검색
                    students = userRepository.findByRoleAndGroupName(User.Role.STUDENT, teacherGroupName).stream()
                        .filter(student -> student.getName().contains(searchQuery) || 
                                         student.getEmail().contains(searchQuery))
                        .collect(Collectors.toList());
                }
            } else {
                // 전체 학생 목록
                if (isAdmin) {
                    // ADMIN은 모든 학생 조회 가능
                    students = userRepository.findByRole(User.Role.STUDENT);
                } else {
                    // TEACHER는 자신의 group_name에 해당하는 학생만 조회
                    students = userRepository.findByRoleAndGroupName(User.Role.STUDENT, teacherGroupName);
                }
            }

            return students.stream()
                .map(student -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", student.getId());
                    studentMap.put("name", student.getName());
                    studentMap.put("email", student.getEmail());
                    studentMap.put("grade", getStudentGrade(student));
                    studentMap.put("groupName", student.getGroupName());
                    studentMap.put("isActive", student.getIsActive());
                    
                    // 학생의 진행도 정보 추가
                    Map<String, Object> progress = getStudentProgressSummary(student.getId());
                    studentMap.put("progress", progress);
                    
                    return studentMap;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("학생 목록 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 특정 학생의 기본 정보 조회 (권한 확인 포함)
     */
    public Map<String, Object> getStudentInfo(Long studentId, String teacherGroupName, boolean isAdmin) {
        try {
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

            // 권한 확인
            if (!isAdmin && !teacherGroupName.equals(student.getGroupName())) {
                throw new RuntimeException("해당 학생에 대한 접근 권한이 없습니다.");
            }

            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("id", student.getId());
            studentInfo.put("name", student.getName());
            studentInfo.put("email", student.getEmail());
            studentInfo.put("grade", getStudentGrade(student));
            studentInfo.put("groupName", student.getGroupName());
            studentInfo.put("isActive", student.getIsActive());
            studentInfo.put("createdAt", student.getCreatedAt());

            return studentInfo;
        } catch (Exception e) {
            log.error("학생 정보 조회 실패", e);
            throw e;
        }
    }

    /**
     * 학생의 학습 데이터 조회 (달력용)
     */
    public List<Map<String, Object>> getStudentLearningData(Long studentId, String startDate, String endDate, 
                                                           String teacherGroupName, boolean isAdmin) {
        try {
            // 권한 확인
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));
            
            if (!isAdmin && !teacherGroupName.equals(student.getGroupName())) {
                throw new RuntimeException("해당 학생에 대한 접근 권한이 없습니다.");
            }

            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            // 날짜별 학습 데이터 조회 (최적화된 쿼리)
            List<Map<String, Object>> learningData = start.datesUntil(end.plusDays(1))
                .map(date -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.toString());
                    dayData.put("dateFormatted", date.format(DateTimeFormatter.ofPattern("MM/dd")));
                    
                    // 해당 날짜의 단어 학습 수 (최적화된 쿼리)
                    long wordsLearned = userWordProgressRepository.countLearnedWordsByUserIdAndDate(studentId, date);
                    dayData.put("wordsLearned", wordsLearned);
                    
                    // 해당 날짜의 문장 학습 수 (최적화된 쿼리)
                    long sentencesLearned = userSentenceProgressRepository.countCompletedSentencesByUserIdAndDate(studentId, date);
                    dayData.put("sentencesLearned", sentencesLearned);
                    
                    // 해당 날짜의 총 학습량
                    long totalLearned = wordsLearned + sentencesLearned;
                    dayData.put("totalLearned", totalLearned);
                    
                    // 학습 여부 (달력 표시용)
                    dayData.put("hasActivity", totalLearned > 0);
                    
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
     * 학생의 진행도 그래프 데이터 조회
     */
    public Map<String, Object> getStudentProgressGraph(Long studentId, String teacherGroupName, boolean isAdmin) {
        try {
            // 권한 확인
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));
            
            if (!isAdmin && !teacherGroupName.equals(student.getGroupName())) {
                throw new RuntimeException("해당 학생에 대한 접근 권한이 없습니다.");
            }

            Map<String, Object> graphData = new HashMap<>();
            
            // 최근 30일간의 일별 진행도
            List<Map<String, Object>> dailyProgress = LocalDate.now().minusDays(29)
                .datesUntil(LocalDate.now().plusDays(1))
                .map(date -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.toString());
                    dayData.put("dateFormatted", date.format(DateTimeFormatter.ofPattern("MM/dd")));
                    
                    // 해당 날짜의 누적 학습량
                    long cumulativeWords = userWordProgressRepository.countLearnedWordsByUserIdUntilDate(studentId, date);
                    long cumulativeSentences = userSentenceProgressRepository.countCompletedSentencesByUserIdUntilDate(studentId, date);
                    
                    dayData.put("cumulativeWords", cumulativeWords);
                    dayData.put("cumulativeSentences", cumulativeSentences);
                    dayData.put("totalProgress", cumulativeWords + cumulativeSentences);
                    
                    return dayData;
                })
                .collect(Collectors.toList());
            
            graphData.put("dailyProgress", dailyProgress);
            
            // 주간 요약 (최근 4주)
            List<Map<String, Object>> weeklySummary = List.of();
            for (int i = 3; i >= 0; i--) {
                LocalDate weekStart = LocalDate.now().minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                
                long weekWords = userWordProgressRepository.countLearnedWordsByUserIdBetweenDates(studentId, weekStart, weekEnd);
                long weekSentences = userSentenceProgressRepository.countCompletedSentencesByUserIdBetweenDates(studentId, weekStart, weekEnd);
                
                Map<String, Object> weekData = new HashMap<>();
                weekData.put("week", weekStart.format(DateTimeFormatter.ofPattern("MM/dd")) + "~" + weekEnd.format(DateTimeFormatter.ofPattern("MM/dd")));
                weekData.put("words", weekWords);
                weekData.put("sentences", weekSentences);
                weekData.put("total", weekWords + weekSentences);
                
                weeklySummary.add(weekData);
            }
            graphData.put("weeklySummary", weeklySummary);
            
            return graphData;
        } catch (Exception e) {
            log.error("학생 진행도 그래프 데이터 조회 실패", e);
            return new HashMap<>();
        }
    }

    /**
     * 학생 진행도 요약 정보
     */
    private Map<String, Object> getStudentProgressSummary(Long studentId) {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // 전체 학습한 단어 수
            long totalWordsLearned = userWordProgressRepository.countLearnedWordsByUserId(studentId);
            
            // 전체 학습한 문장 수
            long totalSentencesLearned = userSentenceProgressRepository.countCompletedSentencesByUserId(studentId);
            
            // 오늘 학습한 단어 수
            long todayWordsLearned = userWordProgressRepository.countLearnedWordsByUserIdAndDate(studentId, LocalDate.now());
            
            // 오늘 학습한 문장 수
            long todaySentencesLearned = userSentenceProgressRepository.countCompletedSentencesByUserIdAndDate(studentId, LocalDate.now());
            
            // 이번 주 학습량
            LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            long weekWordsLearned = userWordProgressRepository.countLearnedWordsByUserIdBetweenDates(studentId, weekStart, LocalDate.now());
            long weekSentencesLearned = userSentenceProgressRepository.countCompletedSentencesByUserIdBetweenDates(studentId, weekStart, LocalDate.now());
            
            summary.put("totalWords", totalWordsLearned);
            summary.put("totalSentences", totalSentencesLearned);
            summary.put("todayWords", todayWordsLearned);
            summary.put("todaySentences", todaySentencesLearned);
            summary.put("weekWords", weekWordsLearned);
            summary.put("weekSentences", weekSentencesLearned);
            summary.put("totalProgress", totalWordsLearned + totalSentencesLearned);
            
            return summary;
        } catch (Exception e) {
            log.error("학생 진행도 요약 조회 실패", e);
            return new HashMap<>();
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
    public Map<String, Object> getStudentStats(Long studentId, String teacherGroupName, boolean isAdmin) {
        try {
            // 권한 확인
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));
            
            if (!isAdmin && !teacherGroupName.equals(student.getGroupName())) {
                throw new RuntimeException("해당 학생에 대한 접근 권한이 없습니다.");
            }

            Map<String, Object> stats = new HashMap<>();
            
            // 전체 학습한 단어 수
            long totalWordsLearned = userWordProgressRepository.countLearnedWordsByUserId(studentId);
            
            // 전체 학습한 문장 수
            long totalSentencesLearned = userSentenceProgressRepository.countCompletedSentencesByUserId(studentId);
            
            // 오늘 학습한 단어 수
            long todayWordsLearned = userWordProgressRepository.countLearnedWordsByUserIdAndDate(studentId, LocalDate.now());
            
            // 오늘 학습한 문장 수
            long todaySentencesLearned = userSentenceProgressRepository.countCompletedSentencesByUserIdAndDate(studentId, LocalDate.now());
            
            // 이번 주 학습량
            LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            long weekWordsLearned = userWordProgressRepository.countLearnedWordsByUserIdBetweenDates(studentId, weekStart, LocalDate.now());
            long weekSentencesLearned = userSentenceProgressRepository.countCompletedSentencesByUserIdBetweenDates(studentId, weekStart, LocalDate.now());
            
            stats.put("totalWordsLearned", totalWordsLearned);
            stats.put("totalSentencesLearned", totalSentencesLearned);
            stats.put("todayWordsLearned", todayWordsLearned);
            stats.put("todaySentencesLearned", todaySentencesLearned);
            stats.put("weekWordsLearned", weekWordsLearned);
            stats.put("weekSentencesLearned", weekSentencesLearned);
            stats.put("totalCoins", 0); // TODO: 코인 히스토리 구현 시 수정
            
            return stats;
        } catch (Exception e) {
            log.error("학생 통계 조회 실패", e);
            return new HashMap<>();
        }
    }

    // 학생의 Day/Level별 학습 진행도 데이터 조회
    public List<DayLevelProgressDto> getStudentDayLevelProgress(Long studentId) {
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

        // Day/Level별 단어 학습 진행도
        List<Object[]> wordProgress = userWordProgressRepository.findDayLevelProgressByUserId(studentId);
        log.info("단어 진행도 데이터: {}", wordProgress);
        for (Object[] row : wordProgress) {
            log.info("단어 진행도 상세: Day={}, Level={}, Count={}", row[0], row[1], row[2]);
        }
        
        // Day/Level별 문장 학습 진행도
        List<Object[]> sentenceProgress = userSentenceProgressRepository.findDayLevelProgressByUserId(studentId);
        log.info("문장 진행도 데이터: {}", sentenceProgress);
        for (Object[] row : sentenceProgress) {
            log.info("문장 진행도 상세: Day={}, Level={}, Count={}", row[0], row[1], row[2]);
        }

        // 결과를 Map으로 변환하여 통합
        Map<String, DayLevelProgressDto> progressMap = new HashMap<>();

        // 단어 진행도 처리
        for (Object[] row : wordProgress) {
            Integer day = (Integer) row[0];
            Integer level = (Integer) row[1];
            Long count = (Long) row[2];
            
            String key = day + "_" + level;
            DayLevelProgressDto dto = progressMap.computeIfAbsent(key, k -> {
                DayLevelProgressDto newDto = new DayLevelProgressDto();
                newDto.setDay(day);
                newDto.setLevel(level);
                newDto.setWordsLearned(0L);
                newDto.setSentencesLearned(0L);
                return newDto;
            });
            dto.setWordsLearned(count);
        }

        // 문장 진행도 처리 (dayNumber -> day, difficultyLevel -> level로 매핑)
        for (Object[] row : sentenceProgress) {
            Integer dayNumber = (Integer) row[0];
            Integer difficultyLevel = (Integer) row[1];
            Long count = (Long) row[2];
            
            String key = dayNumber + "_" + difficultyLevel;
            DayLevelProgressDto dto = progressMap.computeIfAbsent(key, k -> {
                DayLevelProgressDto newDto = new DayLevelProgressDto();
                newDto.setDay(dayNumber);
                newDto.setLevel(difficultyLevel);
                newDto.setWordsLearned(0L);
                newDto.setSentencesLearned(0L);
                return newDto;
            });
            dto.setSentencesLearned(count);
        }

        // 정렬 (Day, Level 순)
        List<DayLevelProgressDto> result = progressMap.values().stream()
            .sorted(Comparator.comparing(DayLevelProgressDto::getDay)
                .thenComparing(DayLevelProgressDto::getLevel))
            .collect(Collectors.toList());
            
        log.info("최종 Day/Level 진행도 데이터: {}", result);
        return result;
    }

    /**
     * 학생 학년 계산 (임시 로직)
     */
    private String getStudentGrade(User student) {
        // 실제로는 학생의 학년 정보가 별도 필드로 관리되어야 함
        // 현재는 group_name에서 추출하거나 기본값 반환
        if (student.getGroupName() != null && student.getGroupName().contains("학년")) {
            return student.getGroupName();
        }
        return "학년 정보 없음";
    }

    private CustomUserPrincipal getCurrentUser() {
        return (CustomUserPrincipal) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
} 