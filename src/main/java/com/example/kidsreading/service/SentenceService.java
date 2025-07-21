package com.example.kidsreading.service;

import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.entity.Sentence;
import com.example.kidsreading.entity.UserSentenceProgress;
import com.example.kidsreading.repository.SentenceRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SentenceService {

    private final SentenceRepository sentenceRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;
    private final S3Service s3Service;
    private final StreakService streakService;
    private final BadgeEarningService badgeEarningService;

    /**
     * 특정 레벨과 날짜의 문장 목록 조회
     */
    public List<SentenceDto> getSentencesByLevelAndDay(Integer level, Integer day) {
        List<Sentence> sentences;

        if (level == 0 && day == 0) {
            // 모든 레벨, 모든 Day
            sentences = sentenceRepository.findByIsActiveTrueOrderByDifficultyLevelAscDayNumberAsc();
        } else if (level == 0) {
            // 모든 레벨, 특정 Day
            sentences = sentenceRepository.findByDayNumberAndIsActiveTrueOrderByDifficultyLevelAsc(day);
        } else if (day == 0) {
            // 특정 레벨, 모든 Day
            sentences = sentenceRepository.findByDifficultyLevelAndIsActiveTrueOrderByDayNumberAsc(level);
        } else {
            // 특정 레벨, 특정 Day
            sentences = sentenceRepository.findByDifficultyLevelAndDayNumberAndIsActiveTrueOrderByDisplayOrderAscIdAsc(level, day);
        }

        return sentences.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 문장 학습 진행상황 업데이트 (Upsert: insert or update)
     */
    @Transactional
    public void updateSentenceProgress(Long userId, Long sentenceId, Boolean isCompleted, Boolean isFirstTime, String email) {
        UserSentenceProgress progress = userSentenceProgressRepository
                .findByUserIdAndSentenceId(userId, sentenceId)
                .orElseGet(() -> {
                    UserSentenceProgress newProgress = UserSentenceProgress.builder()
                            .userId(userId)
                            .sentenceId(sentenceId)
                            .isCompleted(false)
                            .hasRecording(false) // ← 기본값 false로 명시!
                            .isLearned(false) // ← 기본값 false로 명시!
                            .learnCount(0)
                            .createdAt(java.time.LocalDateTime.now())
                            .email(email)
                            .build();
                    // 최초 학습 시각 기록
                    newProgress.setFirstLearnedAt(java.time.LocalDateTime.now());
                    return newProgress;
                });
        progress.setIsCompleted(isCompleted);
        if (Boolean.TRUE.equals(isCompleted)) {
            progress.setIsLearned(true);
        }
        progress.setLastLearnedAt(java.time.LocalDateTime.now());
        progress.setUpdatedAt(java.time.LocalDateTime.now());
        progress.setEmail(email);

        // learn_count 증가 (복습 시에도 증가)
        if (isCompleted != null && isCompleted) {
            progress.setLearnCount(progress.getLearnCount() + 1);
        }

        // first_learned_at이 null이면 현재 시간으로 설정
        if (progress.getFirstLearnedAt() == null) {
            progress.setFirstLearnedAt(java.time.LocalDateTime.now());
        }

        // 기존 progress에도 null일 경우 false로 보정
        if (progress.getHasRecording() == null) {
            progress.setHasRecording(false);
        }
        if (progress.getIsLearned() == null) {
            progress.setIsLearned(false);
        }

        userSentenceProgressRepository.save(progress);

        // 학습 완료 시 연속 학습일 업데이트
        if (isCompleted != null && isCompleted) {
            streakService.updateStreak(userId);
            // 뱃지 체크
            badgeEarningService.checkBadgesOnSentenceCompletion(userId);
        }
    }

    /**
     * 완료된 문장 수 조회
     */
    public int getCompletedSentencesCount(Long userId, Integer level, Integer day) {
        return userSentenceProgressRepository
                .countByUserIdAndSentence_DifficultyLevelAndSentence_DayNumberAndIsCompletedTrue(
                        userId,
                        level,
                        day
                );
    }

    /**
     * 전체 문장 수 조회
     */
    public int getTotalSentencesCount(Integer level, Integer day) {
        return sentenceRepository.countByDifficultyLevelAndDayNumberAndIsActive(level, day, true);
    }

    @Transactional(readOnly = false)
    public int bulkUploadFromExcel(MultipartFile file) throws Exception {
        int count = 0;
        // day별 카운터
        java.util.Map<Integer, Integer> dayCounter = new java.util.HashMap<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            boolean isFirstRow = true;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isFirstRow) { // 헤더는 건너뜀
                    isFirstRow = false;
                    continue;
                }
                String sentence = getCellValue(row.getCell(0));
                String meaning = getCellValue(row.getCell(1));
                int level = getNumericCellValueSafely(row.getCell(3), 1);
                int day = getNumericCellValueSafely(row.getCell(4), 1);
                int counter = dayCounter.getOrDefault(day, 1);
                // audioUrl을 폴더 경로로만 저장 (파일명/확장자 없이) - sentence/ 접두사 추가
                String audioUrl = String.format("sentence/level%d/day%d/", level, day);
                dayCounter.put(day, counter + 1);
                Sentence sentenceEntity = Sentence.builder()
                        .englishText(sentence)
                        .koreanTranslation(meaning)
                        .audioUrl(audioUrl)
                        .difficultyLevel(level)
                        .dayNumber(day)
                        .isActive(true)
                        .build();
                if (sentenceEntity.getAudioUrl() == null || sentenceEntity.getAudioUrl().isEmpty() || !sentenceEntity.getAudioUrl().startsWith("http")) {
                    sentenceEntity.setAudioUrl(audioUrl);
                }
                sentenceRepository.save(sentenceEntity);
                count++;
            }
        }
        return count;
    }

    @Transactional(readOnly = false)
    public Map<String, Object> bulkUploadSentenceAudio(MultipartFile file) throws Exception {
        int successCount = 0, errorCount = 0;
        java.util.List<String> successFiles = new java.util.ArrayList<>();
        java.util.List<String> errorFiles = new java.util.ArrayList<>();
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SentenceService.class);
        // 1. ZIP 내 모든 파일을 폴더별로 분류 (String 파일명 기준)
        java.util.Map<String, java.util.List<String>> folderFiles = new java.util.HashMap<>();
        java.util.zip.ZipInputStream zis1 = new java.util.zip.ZipInputStream(file.getInputStream());
        java.util.zip.ZipEntry entry1;
        while ((entry1 = zis1.getNextEntry()) != null) {
            if (!entry1.isDirectory()) {
                String filePath = entry1.getName().replace("\\", "/");
                int lastSlash = filePath.lastIndexOf('/');
                String folder = (lastSlash > 0) ? filePath.substring(0, lastSlash + 1) : "";
                folderFiles.computeIfAbsent(folder, k -> new java.util.ArrayList<>()).add(filePath);
            }
        }
        zis1.close();
        // 2. 폴더별로 파일/문장 매칭
        for (String folder : folderFiles.keySet()) {
            java.util.List<String> fileList = folderFiles.get(folder);
            java.util.Collections.sort(fileList);
            // audioUrl이 해당 폴더로 시작하는 문장만 추출
            java.util.List<Sentence> sentenceList = sentenceRepository.findByAudioUrlStartingWith(folder);
            // 정렬(등록순)
            sentenceList.sort(java.util.Comparator.comparing(Sentence::getId));
            int matchCount = Math.min(fileList.size(), sentenceList.size());
            // ZIP 파일을 다시 읽어서 해당 파일만 추출
            for (int i = 0; i < matchCount; i++) {
                String filePath = fileList.get(i);
                Sentence matchingSentence = sentenceList.get(i);
                // ZIP에서 해당 파일 추출
                java.util.zip.ZipInputStream zis2 = new java.util.zip.ZipInputStream(file.getInputStream());
                java.util.zip.ZipEntry entry2;
                File tempFile = null;
                while ((entry2 = zis2.getNextEntry()) != null) {
                    String entryPath = entry2.getName().replace("\\", "/");
                    if (!entry2.isDirectory() && entryPath.equals(filePath)) {
                        tempFile = File.createTempFile("audio_", null);
                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis2.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        break;
                    }
                }
                zis2.close();
                if (tempFile == null) {
                    errorCount++;
                    errorFiles.add(filePath + " (ZIP 파일에서 추출 실패)");
                    continue;
                }
                try {
                    // S3 업로드 (폴더+파일명 그대로)
                    String s3Key = filePath;
                    String uploadedKey = s3Service.uploadFileWithKey(tempFile, s3Key);
                    String s3Url = s3Service.getS3Url(uploadedKey);

                    // audioUrl에 S3 URL 저장
                    matchingSentence.setAudioUrl(s3Url);
                    sentenceRepository.save(matchingSentence);
                    sentenceRepository.flush(); // 즉시 DB 반영
                    log.info("[DB:후] sentenceId={}, audioUrl={}", matchingSentence.getId(), matchingSentence.getAudioUrl());
                } catch (Exception e) {
                    log.error("[ERROR] sentenceId={}, S3 URL 저장 실패: {}", matchingSentence.getId(), e.getMessage(), e);
                    errorCount++;
                    errorFiles.add(filePath + " (DB 저장 실패): " + e.getMessage());
                    continue;
                }
                tempFile.delete();
                successCount++;
                successFiles.add(filePath);
            }
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("successFiles", successFiles);
        result.put("errorFiles", errorFiles);
        return result;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    private int getNumericCellValueSafely(Cell cell, int defaultValue) {
        if (cell == null) return defaultValue;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 오늘 완료된 문장 수 조회
     */
    public int getTodayCompletedSentencesCount(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        return userSentenceProgressRepository.countCompletedSentencesByUserAndDateRange(
            userId, startOfDay, endOfDay);
    }

    /**
     * 어제 완료된 문장 수 조회
     */
    public int getYesterdayCompletedSentencesCount(Long userId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);
        
        return userSentenceProgressRepository.countCompletedSentencesByUserAndDateRange(
            userId, startOfDay, endOfDay);
    }

    /**
     * 특정 날짜에 완료된 문장 수 조회
     */
    public int getCompletedSentencesCountByDate(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        return userSentenceProgressRepository.countCompletedSentencesByUserAndDateRange(
            userId, startOfDay, endOfDay);
    }

    /**
     * Sentence 엔티티를 SentenceDto로 변환
     */
    private SentenceDto convertToDto(Sentence sentence) {
        return SentenceDto.builder()
                .id(sentence.getId())
                .english(sentence.getEnglishText()) // englishText를 english로 매핑
                .korean(sentence.getKoreanTranslation()) // koreanTranslation을 korean으로 매핑
                .level(sentence.getDifficultyLevel())
                .dayNumber(sentence.getDayNumber())
                .audioUrl(sentence.getAudioUrl())
                .translation(sentence.getKoreanTranslation())
                .meaning(sentence.getKoreanTranslation())
                .isActive(sentence.getIsActive())
                .build();
    }
}