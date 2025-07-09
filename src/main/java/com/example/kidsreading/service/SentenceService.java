package com.example.kidsreading.service;

import com.example.kidsreading.dto.BulkUploadResultDto;
import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.entity.Sentence;
import com.example.kidsreading.entity.UserSentenceProgress;
import com.example.kidsreading.repository.SentenceRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SentenceService {
    private final SentenceRepository sentenceRepository;
    private final AdminService adminService;
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
     * 완료된 문장 수 조회 (특정 레벨/Day) - Long 타입 userId
     */
    public int getCompletedSentencesCount(Long userId, Integer level, Integer day) {
        return (int) userSentenceProgressRepository.countByUserIdAndSentence_DifficultyLevelAndSentence_DayNumberAndIsCompletedTrue(
                userId,
                level,
                day
        );
    }

    /**
     * 완료된 문장 수 조회 (특정 레벨/Day) - String 타입 userId
     */
    public int getCompletedSentencesCountByUserId(String userId, Integer level, Integer day) {
        return (int) userSentenceProgressRepository.countByUserIdStringAndSentence_DifficultyLevelAndSentence_DayNumberAndIsCompletedTrue(
                userId,
                level,
                day
        );
    }

    /**
     * 전체 문장 수 조회 (특정 레벨/Day)
     */
    public int getTotalSentencesCount(Integer level, Integer day) {
        return (int) sentenceRepository.countByDifficultyLevelAndDayNumberAndIsActiveTrue(level, day);
    }

    /**
     * Entity를 DTO로 변환
     */
    private SentenceDto convertToDto(Sentence sentence) {
        return SentenceDto.builder()
                .id(sentence.getId())
                .text(sentence.getText())
                .meaning(sentence.getTranslation())
                .level(sentence.getDifficultyLevel())
                .day(sentence.getDayNumber())
                .audioUrl(sentence.getAudioUrl())
                .isActive(sentence.getIsActive())
                .build();
    }

    /**
     * 엑셀 파일로부터 문장 데이터 일괄 업로드
     */
    @Transactional
    public void uploadSentencesFromExcel(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 헤더 행 건너뛰기
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (isRowEmpty(row)) continue;

                String text = getCellValueAsString(row.getCell(0));
                String translation = getCellValueAsString(row.getCell(1));
                Integer difficultyLevel = getCellValueAsInteger(row.getCell(2));
                Integer dayNumber = getCellValueAsInteger(row.getCell(3));

                if (text != null && !text.trim().isEmpty() && 
                    difficultyLevel != null && dayNumber != null) {

                    Sentence sentence = Sentence.builder()
                            .text(text.trim())
                            .translation(translation != null ? translation.trim() : "")
                            .difficultyLevel(difficultyLevel)
                            .dayNumber(dayNumber)
                            .isActive(true)
                            .build();

                    sentenceRepository.save(sentence);
                }
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 4; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}