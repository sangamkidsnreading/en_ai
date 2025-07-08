// ========== SentenceService.java ==========
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SentenceService {

    private final SentenceRepository sentenceRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;
    private final S3Service s3Service;

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
            sentences = sentenceRepository.findByDifficultyLevelAndDayNumberAndIsActiveTrue(level, day);
        }

        return sentences.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 문장 학습 진행상황 업데이트
     */
    @Transactional
    public void updateSentenceProgress(Long userId, Long sentenceId, Boolean isCompleted) {
        UserSentenceProgress progress = userSentenceProgressRepository
                .findByUserIdAndSentenceId(userId, sentenceId)
                .orElse(UserSentenceProgress.builder()
                        .userId(userId)
                        .sentenceId(sentenceId)
                        .isCompleted(false)
                        .build());

        progress.setIsCompleted(isCompleted);
        userSentenceProgressRepository.save(progress);
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
                String english = getCellValue(row.getCell(0));
                String korean = getCellValue(row.getCell(1));
                String audioFile = getCellValue(row.getCell(2));
                int level = getNumericCellValueSafely(row.getCell(3), 1);
                int day = getNumericCellValueSafely(row.getCell(4), 1);

                // S3 URL 생성 (오디오 파일명이 있는 경우)
                String s3AudioUrl = null;
                if (audioFile != null && !audioFile.trim().isEmpty()) {
                    // S3 키 생성 (예: vocabulary/2025/07-08/sentences/1Day - We are happy.wav)
                    String s3Key = s3Service.buildS3Key("sentences", audioFile);
                    s3AudioUrl = s3Service.getS3Url(s3Key);
                }

                Sentence sentence = Sentence.builder()
                        .englishText(english)
                        .koreanTranslation(korean)
                        .audioUrl(s3AudioUrl)
                        .difficultyLevel(level)
                        .dayNumber(day)
                        .isActive(true)
                        .build();
                sentenceRepository.save(sentence);
                count++;
            }
        }
        return count;
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