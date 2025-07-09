//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.kidsreading.service;

import com.example.kidsreading.dto.AdminStatsDto;
import com.example.kidsreading.dto.LearningSettingsDto;
import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.UserDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.entity.LearningSettings;
import com.example.kidsreading.entity.Sentence;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.entity.Word;
import com.example.kidsreading.entity.User.Role;
import com.example.kidsreading.repository.LearningSettingsRepository;
import com.example.kidsreading.repository.SentenceRepository;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.WordRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;
import com.example.kidsreading.service.S3Service;

@Service
public class AdminService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final SentenceRepository sentenceRepository;
    private final LearningSettingsRepository learningSettingsRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;
    private final S3Service s3Service;

    public AdminStatsDto getAdminStats() {
        try {
            long totalUsers = this.userRepository.count();
            long totalWords = this.wordRepository.countByIsActiveTrue();
            long totalSentences = this.sentenceRepository.countByIsActiveTrue();
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7L);
            long activeUsers = this.userWordProgressRepository.countDistinctUsersByLastStudiedAtAfter(weekAgo);
            return AdminStatsDto.builder().totalUsers(totalUsers).totalWords(totalWords).totalSentences(totalSentences).activeUsers(activeUsers).build();
        } catch (Exception e) {
            log.error("통계 조회 중 오류 발생", e);
            return AdminStatsDto.builder().totalUsers(0L).totalWords(0L).totalSentences(0L).activeUsers(0L).build();
        }
    }

    public List<UserDto> getAllUsers() {
        try {
            List<User> users = this.userRepository.findAll();
            return (List)users.stream().map(this::convertToUserDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return new ArrayList();
        }
    }

    public UserDto createUser(UserDto userDto) {
        try {
            if (this.userRepository.existsByUsername(userDto.getUsername())) {
                throw new RuntimeException("이미 존재하는 사용자명입니다: " + userDto.getUsername());
            } else if (this.userRepository.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("이미 존재하는 이메일입니다: " + userDto.getEmail());
            } else {
                User user = User.builder().username(userDto.getUsername()).email(userDto.getEmail()).name(userDto.getName()).password(userDto.getPassword()).role(Role.valueOf(userDto.getRole())).isActive(true).build();
                User savedUser = (User)this.userRepository.save(user);
                return this.convertToUserDto(savedUser);
            }
        } catch (Exception e) {
            log.error("사용자 생성 실패", e);
            throw new RuntimeException("사용자 생성에 실패했습니다: " + e.getMessage());
        }
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        try {
            User user = (User)this.userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setName(userDto.getName());
            user.setRole(Role.valueOf(userDto.getRole()));
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(userDto.getPassword());
            }

            User savedUser = (User)this.userRepository.save(user);
            return this.convertToUserDto(savedUser);
        } catch (Exception e) {
            log.error("사용자 수정 실패", e);
            throw new RuntimeException("사용자 수정에 실패했습니다.");
        }
    }

    public void deleteUser(Long userId) {
        try {
            this.userRepository.deleteById(userId);
        } catch (Exception e) {
            log.error("사용자 삭제 실패", e);
            throw new RuntimeException("사용자 삭제에 실패했습니다.");
        }
    }

    public List<UserDto> searchUsers(String query) {
        try {
            List<User> users = this.userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(query, query, query);
            return (List)users.stream().map(this::convertToUserDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 검색 실패", e);
            return new ArrayList();
        }
    }

    public List<WordDto> getAllWords() {
        try {
            List<Word> words = this.wordRepository.findByIsActiveTrue();
            return (List)words.stream().map(this::convertToWordDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("단어 목록 조회 실패", e);
            return new ArrayList();
        }
    }

    public WordDto createWord(WordDto wordDto) {
        try {
            Word word = Word.builder()
                .text(wordDto.getEnglish())
                .meaning(wordDto.getKorean())
                .level(wordDto.getLevel())
                .day(wordDto.getDay() != null ? wordDto.getDay() : 1)
                .pronunciation(wordDto.getPronunciation())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            Word savedWord = (Word)this.wordRepository.save(word);
            return this.convertToWordDto(savedWord);
        } catch (Exception e) {
            log.error("단어 생성 실패", e);
            throw new RuntimeException("단어 생성에 실패했습니다.");
        }
    }

    public WordDto updateWord(Long wordId, WordDto wordDto) {
        try {
            Word word = (Word)this.wordRepository.findById(wordId).orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
            word.setText(wordDto.getEnglish());
            word.setMeaning(wordDto.getKorean());
            word.setLevel(wordDto.getLevel());
            word.setDay(wordDto.getDay() != null ? wordDto.getDay() : word.getDay());
            word.setPronunciation(wordDto.getPronunciation());
            Word savedWord = (Word)this.wordRepository.save(word);
            return this.convertToWordDto(savedWord);
        } catch (Exception e) {
            log.error("단어 수정 실패", e);
            throw new RuntimeException("단어 수정에 실패했습니다.");
        }
    }

    public void deleteWord(Long wordId) {
        try {
            Word word = (Word)this.wordRepository.findById(wordId).orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
            word.setIsActive(false);
            this.wordRepository.save(word);
        } catch (Exception e) {
            log.error("단어 삭제 실패", e);
            throw new RuntimeException("단어 삭제에 실패했습니다.");
        }
    }

    public List<WordDto> searchWords(String query) {
        try {
            List<Word> words = this.wordRepository.searchByQuery(query);
            return (List)words.stream().map(this::convertToWordDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("단어 검색 실패", e);
            return new ArrayList();
        }
    }

    public List<WordDto> getWordsByLevel(Integer level) {
        try {
            List<Word> words = this.wordRepository.findByLevelAndIsActiveTrue(level);
            return (List)words.stream().map(this::convertToWordDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("레벨별 단어 조회 실패", e);
            return new ArrayList();
        }
    }

    public List<SentenceDto> getAllSentences() {
        try {
            List<Sentence> sentences = this.sentenceRepository.findByIsActiveTrue();
            return (List)sentences.stream().map(this::convertToSentenceDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("문장 목록 조회 실패", e);
            return new ArrayList();
        }
    }

    public SentenceDto createSentence(SentenceDto sentenceDto) {
        try {
            Sentence sentence = Sentence.builder()
                .englishText(sentenceDto.getEnglish())
                .koreanTranslation(sentenceDto.getKorean())
                .difficultyLevel(sentenceDto.getLevel())
                .dayNumber(sentenceDto.getDayNumber() != null ? sentenceDto.getDayNumber() : 1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            Sentence savedSentence = (Sentence)this.sentenceRepository.save(sentence);
            return this.convertToSentenceDto(savedSentence);
        } catch (Exception e) {
            log.error("문장 생성 실패", e);
            throw new RuntimeException("문장 생성에 실패했습니다.");
        }
    }

    public SentenceDto updateSentence(Long sentenceId, SentenceDto sentenceDto) {
        try {
            Sentence sentence = (Sentence)this.sentenceRepository.findById(sentenceId).orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));
            sentence.setEnglishText(sentenceDto.getEnglish());
            sentence.setKoreanTranslation(sentenceDto.getKorean());
            sentence.setDifficultyLevel(sentenceDto.getLevel());
            sentence.setDayNumber(sentenceDto.getDayNumber() != null ? sentenceDto.getDayNumber() : sentence.getDayNumber());
            Sentence savedSentence = (Sentence)this.sentenceRepository.save(sentence);
            return this.convertToSentenceDto(savedSentence);
        } catch (Exception e) {
            log.error("문장 수정 실패", e);
            throw new RuntimeException("문장 수정에 실패했습니다.");
        }
    }

    public void deleteSentence(Long sentenceId) {
        try {
            Sentence sentence = (Sentence)this.sentenceRepository.findById(sentenceId).orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));
            sentence.setIsActive(false);
            this.sentenceRepository.save(sentence);
        } catch (Exception e) {
            log.error("문장 삭제 실패", e);
            throw new RuntimeException("문장 삭제에 실패했습니다.");
        }
    }

    public List<SentenceDto> searchSentences(String query) {
        try {
            List<Sentence> sentences = this.sentenceRepository.searchByQuery(query);
            return (List)sentences.stream().map(this::convertToSentenceDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("문장 검색 실패", e);
            return new ArrayList();
        }
    }

    public List<SentenceDto> getSentencesByLevel(Integer level) {
        try {
            List<Sentence> sentences = this.sentenceRepository.findByDifficultyLevelAndIsActiveTrue(level);
            return (List)sentences.stream().map(this::convertToSentenceDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("레벨별 문장 조회 실패", e);
            return new ArrayList();
        }
    }

    public LearningSettingsDto getLearningSettings() {
        try {
            LearningSettings settings = (LearningSettings)this.learningSettingsRepository.findTopByOrderByCreatedAtDesc().orElse(this.getDefaultSettings());
            return this.convertToLearningSettingsDto(settings);
        } catch (Exception e) {
            log.error("학습 설정 조회 실패", e);
            return this.getDefaultLearningSettingsDto();
        }
    }

    public LearningSettingsDto saveLearningSettings(LearningSettingsDto settingsDto) {
        try {
            LearningSettings settings = LearningSettings.builder().audioSpeed(settingsDto.getAudioSpeed()).voiceSpeed(settingsDto.getVoiceSpeed()).repeatCount(settingsDto.getRepeatCount()).wordCoin(settingsDto.getWordCoin()).sentenceCoin(settingsDto.getSentenceCoin()).streakBonus(settingsDto.getStreakBonus()).levelUpCoin(settingsDto.getLevelUpCoin()).maxLevel(settingsDto.getMaxLevel()).dailyWordGoal(settingsDto.getDailyWordGoal()).dailySentenceGoal(settingsDto.getDailySentenceGoal()).build();
            LearningSettings savedSettings = (LearningSettings)this.learningSettingsRepository.save(settings);
            return this.convertToLearningSettingsDto(savedSettings);
        } catch (Exception e) {
            log.error("학습 설정 저장 실패", e);
            throw new RuntimeException("학습 설정 저장에 실패했습니다.");
        }
    }

    public LearningSettingsDto updateLearningSettings(LearningSettingsDto settingsDto) {
        return this.saveLearningSettings(settingsDto);
    }

    public String uploadWordAudio(Long wordId, MultipartFile file) {
        try {
            log.info("단어 음성 파일 S3 업로드: wordId={}, filename={}", wordId, file.getOriginalFilename());
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || 
                (!originalFilename.toLowerCase().endsWith(".mp3") && 
                 !originalFilename.toLowerCase().endsWith(".m4a") &&
                 !originalFilename.toLowerCase().endsWith(".wav") &&
                 !originalFilename.toLowerCase().endsWith(".ogg"))) {
                throw new RuntimeException("MP3, M4A, WAV, OGG 파일만 업로드 가능합니다.");
            }

            // S3에 파일 업로드
            String s3Key = s3Service.uploadFile(file, "words");

            // S3 URL 생성 (직접 URL 구성)
            String s3Url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", 
                    "kidsnreading-sounds", s3Key);

            // 파일이 실제로 업로드되었는지 확인
            if (!s3Service.fileExists(s3Key)) {
                throw new RuntimeException("S3 파일 업로드 검증 실패");
            }

            // 데이터베이스 업데이트
            Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
            word.setAudioUrl(s3Url);
            wordRepository.save(word);

            log.info("단어 음성 파일 S3 업로드 완료: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("단어 음성 파일 S3 업로드 실패: wordId={}", wordId, e);
            throw new RuntimeException("음성 파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    public String uploadSentenceAudio(Long sentenceId, MultipartFile file) {
        try {
            log.info("문장 음성 파일 S3 업로드: sentenceId={}, filename={}", sentenceId, file.getOriginalFilename());
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || 
                (!originalFilename.toLowerCase().endsWith(".mp3") && 
                 !originalFilename.toLowerCase().endsWith(".m4a") &&
                 !originalFilename.toLowerCase().endsWith(".wav") &&
                 !originalFilename.toLowerCase().endsWith(".ogg"))) {
                throw new RuntimeException("MP3, M4A, WAV, OGG 파일만 업로드 가능합니다.");
            }

            // S3에 파일 업로드
            String s3Key = s3Service.uploadFile(file, "sentences");
            String s3Url = s3Service.getS3Url(s3Key);

            // 데이터베이스 업데이트
            Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));
            sentence.setAudioUrl(s3Url);
            sentenceRepository.save(sentence);

            log.info("문장 음성 파일 S3 업로드 완료: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("문장 음성 파일 S3 업로드 실패: sentenceId={}", sentenceId, e);
            throw new RuntimeException("음성 파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteWordAudio(Long wordId) {
        try {
            log.info("단어 음성 파일 S3 삭제: wordId={}", wordId);

            Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

            if (word.getAudioUrl() != null && !word.getAudioUrl().isEmpty()) {
                // S3에서 파일 삭제 (S3 URL에서 키 추출)
                if (word.getAudioUrl().contains("amazonaws.com")) {
                    String s3Key = extractS3KeyFromUrl(word.getAudioUrl());
                    s3Service.deleteFile(s3Key);
                    log.info("단어 음성 파일 S3 삭제 완료: {}", s3Key);
                }

                // 데이터베이스에서 오디오 URL 제거
                word.setAudioUrl(null);
                wordRepository.save(word);
            }
        } catch (Exception e) {
            log.error("단어 음성 파일 S3 삭제 실패: wordId={}", wordId, e);
            throw new RuntimeException("음성 파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteSentenceAudio(Long sentenceId) {
        try {
            log.info("문장 음성 파일 S3 삭제: sentenceId={}", sentenceId);

            Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));

            if (sentence.getAudioUrl() != null && !sentence.getAudioUrl().isEmpty()) {
                // S3에서 파일 삭제 (S3 URL에서 키 추출)
                if (sentence.getAudioUrl().contains("amazonaws.com")) {
                    String s3Key = extractS3KeyFromUrl(sentence.getAudioUrl());
                    s3Service.deleteFile(s3Key);
                    log.info("문장 음성 파일 S3 삭제 완료: {}", s3Key);
                }

                // 데이터베이스에서 오디오 URL 제거
                sentence.setAudioUrl(null);
                sentenceRepository.save(sentence);
            }
        } catch (Exception e) {
            log.error("문장 음성 파일 S3 삭제 실패: sentenceId={}", sentenceId, e);
            throw new RuntimeException("음성 파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> bulkUploadWords(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        List<String> errorRows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 헤더 읽기
            if (!rowIterator.hasNext()) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("엑셀에 데이터가 없습니다."));
                return result;
            }
            Row headerRow = rowIterator.next();
            String wordHeader = headerRow.getCell(0).getStringCellValue().trim();
            String meaningHeader = headerRow.getCell(1).getStringCellValue().trim();
            String audioHeader = headerRow.getCell(2).getStringCellValue().trim();
            String levelHeader = headerRow.getCell(3) != null ? headerRow.getCell(3).getStringCellValue().trim() : null;
            String dayHeader = headerRow.getCell(4) != null ? headerRow.getCell(4).getStringCellValue().trim() : null;

            if (!"word".equalsIgnoreCase(wordHeader) ||
                !"meaning".equalsIgnoreCase(meaningHeader) ||
                !"audio_file".equalsIgnoreCase(audioHeader) ||
                (levelHeader != null && !"level".equalsIgnoreCase(levelHeader)) ||
                (dayHeader != null && !"day".equalsIgnoreCase(dayHeader))) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("헤더명이 올바르지 않습니다. (word, meaning, audio_file, level, day)"));
                return result;
            }

            // 데이터 읽기
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String word = getCellString(row.getCell(0));
                    String meaning = getCellString(row.getCell(1));
                    String audioFile = getCellString(row.getCell(2));
                    Integer level = 1;
                    Integer day = 1;
                    if (row.getCell(3) != null) {
                        String levelStr = getCellString(row.getCell(3));
                        if (levelStr != null && !levelStr.isEmpty()) {
                            try { level = (int) Double.parseDouble(levelStr); } catch (Exception ignore) {}
                        }
                    }
                    if (row.getCell(4) != null) {
                        String dayStr = getCellString(row.getCell(4));
                        if (dayStr != null && !dayStr.isEmpty()) {
                            try { day = (int) Double.parseDouble(dayStr); } catch (Exception ignore) {}
                        }
                    }

                    if (word == null || word.isEmpty() || meaning == null || meaning.isEmpty()) {
                        errorRows.add("단어 또는 의미가 비어있습니다. (행: " + (row.getRowNum() + 1) + ")");
                        errorCount++;
                        continue;
                    }

                    Word newWord = Word.builder()
                        .text(word)
                        .meaning(meaning)
                        .level(level)
                        .day(day)
                        .pronunciation(null)
                        .isActive(true)
                        .build();

                    if (audioFile != null && !audioFile.isEmpty()) {
                        // S3 URL로 저장
                        String s3Key = s3Service.buildS3Key("words", audioFile);
                        String s3Url = s3Service.getS3Url(s3Key);
                        newWord.setAudioUrl(s3Url);
                    } else {
                        newWord.setAudioUrl(null);
                    }
                    this.wordRepository.save(newWord);
                    successCount++;
                } catch (Exception e) {
                    errorRows.add("행 " + (row.getRowNum() + 1) + " 오류: " + e.getMessage());
                    errorCount++;
                }
            }
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errorRows", errorRows);
        } catch (Exception e) {
            log.error("단어 일괄 업로드 실패", e);
            result.put("successCount", 0);
            result.put("errorCount", 1);
            ((List<String>) result.get("errorRows")).add("파일 읽기 중 오류 발생: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> bulkUploadSentences(MultipartFile file) {
        log.info("문장 일괄 업로드: filename={}", file.getOriginalFilename());
        Map<String, Object> result = new HashMap();
        int successCount = 0;
        int errorCount = 0;
        List<String> errorRows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 헤더 읽기
            if (!rowIterator.hasNext()) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("엑셀에 데이터가 없습니다."));
                return result;
            }
            Row headerRow = rowIterator.next();
            String englishHeader = headerRow.getCell(0).getStringCellValue().trim();
            String koreanHeader = headerRow.getCell(1).getStringCellValue().trim();
            String audioHeader = headerRow.getCell(2).getStringCellValue().trim();
            String levelHeader = headerRow.getCell(3) != null ? headerRow.getCell(3).getStringCellValue().trim() : null;
            String dayHeader = headerRow.getCell(4) != null ? headerRow.getCell(4).getStringCellValue().trim() : null;

            if (!"english".equalsIgnoreCase(englishHeader) ||
                !"korean".equalsIgnoreCase(koreanHeader) ||
                !"audio_file".equalsIgnoreCase(audioHeader) ||
                (levelHeader != null && !"level".equalsIgnoreCase(levelHeader)) ||
                (dayHeader != null && !"day".equalsIgnoreCase(dayHeader))) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("헤더명이 올바르지 않습니다. (english, korean, audio_file, level, day)"));
                return result;
            }

            // 데이터 읽기
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String english = getCellString(row.getCell(0));
                    String korean = getCellString(row.getCell(1));
                    String audioFile = getCellString(row.getCell(2));
                    Integer level = 1;
                    Integer day = 1;
                    if (row.getCell(3) != null) {
                        String levelStr = getCellString(row.getCell(3));
                        if (levelStr != null && !levelStr.isEmpty()) {
                            try { level = (int) Double.parseDouble(levelStr); } catch (Exception ignore) {}
                        }
                    }
                    if (row.getCell(4) != null) {
                        String dayStr = getCellString(row.getCell(4));
                        if (dayStr != null && !dayStr.isEmpty()) {
                            try { day = (int) Double.parseDouble(dayStr); } catch (Exception ignore) {}
                        }
                    }

                    if (english == null || english.isEmpty() || korean == null || korean.isEmpty()) {
                         errorRows.add("영어 또는 한국어가 비어있습니다. (행: " + (row.getRowNum() + 1) + ")");
                         errorCount++;
                         continue;
                    }

                    Sentence newSentence = Sentence.builder()
                        .englishText(english)
                        .koreanTranslation(korean)
                        .difficultyLevel(level)
                        .dayNumber(day)
                        .isActive(true)
                        .build();

                    if (audioFile != null && !audioFile.isEmpty()) {
                        // S3 URL로 저장
                        String s3Key = s3Service.buildS3Key("sentences", audioFile);
                        String s3Url = s3Service.getS3Url(s3Key);
                        newSentence.setAudioUrl(s3Url);
                    } else {
                        newSentence.setAudioUrl(null);
                    }
                    this.sentenceRepository.save(newSentence);
                    successCount++;
                } catch (Exception e) {
                    errorRows.add("행 " + (row.getRowNum() + 1) + " 오류: " + e.getMessage());
                    errorCount++;
                }
            }
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errorRows", errorRows);
        } catch (Exception e) {
            log.error("문장 일괄 업로드 실패", e);
            result.put("successCount", 0);
            result.put("errorCount", 1);
            result.put("errorRows", List.of("파일 읽기 중 오류 발생: " + e.getMessage()));
        }

        return result;
    }

    public Map<String, Object> bulkUploadWordAudio(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0, errorCount = 0;
        List<String> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    try {
                        // 임시 파일로 저장
                        File tempFile = File.createTempFile("audio_", "_" + fileName);
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        // S3 업로드
                        String s3Key = s3Service.uploadFileWithOriginalName(tempFile, "words", fileName);
                        String s3Url = s3Service.getS3Url(s3Key);
                        // DB audioUrl 업데이트 (파일명 매칭)
                        List<Word> words = wordRepository.findByIsActiveTrue();
                        for (Word word : words) {
                            if (word.getAudioUrl() != null && (word.getAudioUrl().endsWith("/" + fileName) || word.getAudioUrl().equals(fileName))) {
                                word.setAudioUrl(s3Url);
                                wordRepository.save(word);
                            }
                        }
                        successFiles.add(fileName);
                        successCount++;
                        tempFile.delete();
                    } catch (Exception e) {
                        errorFiles.add(fileName + ": " + e.getMessage());
                        errorCount++;
                    }
                }
            }
        } catch (Exception e) {
            errorFiles.add("ZIP 해제 실패: " + e.getMessage());
            errorCount++;
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("successFiles", successFiles);
        result.put("errorFiles", errorFiles);
        return result;
    }

    public Map<String, Object> bulkUploadSentenceAudio(MultipartFile file) {
        log.info("문장 음원 일괄 업로드: filename={}", file.getOriginalFilename());
        Map<String, Object> result = new HashMap<>();
        int successCount = 0, errorCount = 0;
        List<String> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    try {
                        // 임시 파일로 저장
                        File tempFile = File.createTempFile("audio_", null);
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        // S3 업로드 (원래 파일명으로)
                        s3Service.uploadFileWithOriginalName(tempFile, "sentences", fileName);
                        successFiles.add(fileName);
                        successCount++;
                        tempFile.delete();
                    } catch (Exception e) {
                        errorFiles.add(fileName + ": " + e.getMessage());
                        errorCount++;
                    }
                }
            }
        } catch (Exception e) {
            errorFiles.add("ZIP 해제 실패: " + e.getMessage());
            errorCount++;
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("successFiles", successFiles);
        result.put("errorFiles", errorFiles);
        return result;
    }

    public List<Map<String, Object>> getUserProgress() {
        return new ArrayList();
    }

    public List<Map<String, Object>> getWordStats() {
        return new ArrayList();
    }

    public Map<String, Object> getDailyStats(String date) {
        return new HashMap();
    }

    private UserDto convertToUserDto(User user) {
        return UserDto.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).name(user.getName()).role(user.getRole().name()).isActive(user.getIsActive()).build();
    }

    private WordDto convertToWordDto(Word word) {
        return WordDto.builder().id(This change involves modifying the convertToSentenceDto method in AdminService.java to ensure correct data mapping from Sentence entity to SentenceDto.
<replit_final_file>
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.kidsreading.service;

import com.example.kidsreading.dto.AdminStatsDto;
import com.example.kidsreading.dto.LearningSettingsDto;
import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.UserDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.entity.LearningSettings;
import com.example.kidsreading.entity.Sentence;
import com.example.kidsreading.entity.User;
import com.example.kidsreading.entity.Word;
import com.example.kidsreading.entity.User.Role;
import com.example.kidsreading.repository.LearningSettingsRepository;
import com.example.kidsreading.repository.SentenceRepository;
import com.example.kidsreading.repository.UserRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.WordRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;
import com.example.kidsreading.service.S3Service;

@Service
public class AdminService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final SentenceRepository sentenceRepository;
    private final LearningSettingsRepository learningSettingsRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserSentenceProgressRepository userSentenceProgressRepository;
    private final S3Service s3Service;

    public AdminStatsDto getAdminStats() {
        try {
            long totalUsers = this.userRepository.count();
            long totalWords = this.wordRepository.countByIsActiveTrue();
            long totalSentences = this.sentenceRepository.countByIsActiveTrue();
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7L);
            long activeUsers = this.userWordProgressRepository.countDistinctUsersByLastStudiedAtAfter(weekAgo);
            return AdminStatsDto.builder().totalUsers(totalUsers).totalWords(totalWords).totalSentences(totalSentences).activeUsers(activeUsers).build();
        } catch (Exception e) {
            log.error("통계 조회 중 오류 발생", e);
            return AdminStatsDto.builder().totalUsers(0L).totalWords(0L).totalSentences(0L).activeUsers(0L).build();
        }
    }

    public List<UserDto> getAllUsers() {
        try {
            List<User> users = this.userRepository.findAll();
            return (List)users.stream().map(this::convertToUserDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return new ArrayList();
        }
    }

    public UserDto createUser(UserDto userDto) {
        try {
            if (this.userRepository.existsByUsername(userDto.getUsername())) {
                throw new RuntimeException("이미 존재하는 사용자명입니다: " + userDto.getUsername());
            } else if (this.userRepository.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("이미 존재하는 이메일입니다: " + userDto.getEmail());
            } else {
                User user = User.builder().username(userDto.getUsername()).email(userDto.getEmail()).name(userDto.getName()).password(userDto.getPassword()).role(Role.valueOf(userDto.getRole())).isActive(true).build();
                User savedUser = (User)this.userRepository.save(user);
                return this.convertToUserDto(savedUser);
            }
        } catch (Exception e) {
            log.error("사용자 생성 실패", e);
            throw new RuntimeException("사용자 생성에 실패했습니다: " + e.getMessage());
        }
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        try {
            User user = (User)this.userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setName(userDto.getName());
            user.setRole(Role.valueOf(userDto.getRole()));
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(userDto.getPassword());
            }

            User savedUser = (User)this.userRepository.save(user);
            return this.convertToUserDto(savedUser);
        } catch (Exception e) {
            log.error("사용자 수정 실패", e);
            throw new RuntimeException("사용자 수정에 실패했습니다.");
        }
    }

    public void deleteUser(Long userId) {
        try {
            this.userRepository.deleteById(userId);
        } catch (Exception e) {
            log.error("사용자 삭제 실패", e);
            throw new RuntimeException("사용자 삭제에 실패했습니다.");
        }
    }

    public List<UserDto> searchUsers(String query) {
        try {
            List<User> users = this.userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(query, query, query);
            return (List)users.stream().map(this::convertToUserDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 검색 실패", e);
            return new ArrayList();
        }
    }

    public List<WordDto> getAllWords() {
        try {
            List<Word> words = this.wordRepository.findByIsActiveTrue();
            return (List)words.stream().map(this::convertToWordDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("단어 목록 조회 실패", e);
            return new ArrayList();
        }
    }

    public WordDto createWord(WordDto wordDto) {
        try {
            Word word = Word.builder()
                .text(wordDto.getEnglish())
                .meaning(wordDto.getKorean())
                .level(wordDto.getLevel())
                .day(wordDto.getDay() != null ? wordDto.getDay() : 1)
                .pronunciation(wordDto.getPronunciation())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            Word savedWord = (Word)this.wordRepository.save(word);
            return this.convertToWordDto(savedWord);
        } catch (Exception e) {
            log.error("단어 생성 실패", e);
            throw new RuntimeException("단어 생성에 실패했습니다.");
        }
    }

    public WordDto updateWord(Long wordId, WordDto wordDto) {
        try {
            Word word = (Word)this.wordRepository.findById(wordId).orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
            word.setText(wordDto.getEnglish());
            word.setMeaning(wordDto.getKorean());
            word.setLevel(wordDto.getLevel());
            word.setDay(wordDto.getDay() != null ? wordDto.getDay() : word.getDay());
            word.setPronunciation(wordDto.getPronunciation());
            Word savedWord = (Word)this.wordRepository.save(word);
            return this.convertToWordDto(savedWord);
        } catch (Exception e) {
            log.error("단어 수정 실패", e);
            throw new RuntimeException("단어 수정에 실패했습니다.");
        }
    }

    public void deleteWord(Long wordId) {
        try {
            Word word = (Word)this.wordRepository.findById(wordId).orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
            word.setIsActive(false);
            this.wordRepository.save(word);
        } catch (Exception e) {
            log.error("단어 삭제 실패", e);
            throw new RuntimeException("단어 삭제에 실패했습니다.");
        }
    }

    public List<WordDto> searchWords(String query) {
        try {
            List<Word> words = this.wordRepository.searchByQuery(query);
            return (List)words.stream().map(this::convertToWordDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("단어 검색 실패", e);
            return new ArrayList();
        }
    }

    public List<WordDto> getWordsByLevel(Integer level) {
        try {
            List<Word> words = this.wordRepository.findByLevelAndIsActiveTrue(level);
            return (List)words.stream().map(this::convertToWordDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("레벨별 단어 조회 실패", e);
            return new ArrayList();
        }
    }

    public List<SentenceDto> getAllSentences() {
        try {
            List<Sentence> sentences = this.sentenceRepository.findByIsActiveTrue();
            return (List)sentences.stream().map(this::convertToSentenceDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("문장 목록 조회 실패", e);
            return new ArrayList();
        }
    }

    public SentenceDto createSentence(SentenceDto sentenceDto) {
        try {
            Sentence sentence = Sentence.builder()
                .englishText(sentenceDto.getEnglish())
                .koreanTranslation(sentenceDto.getKorean())
                .difficultyLevel(sentenceDto.getLevel())
                .dayNumber(sentenceDto.getDayNumber() != null ? sentenceDto.getDayNumber() : 1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            Sentence savedSentence = (Sentence)this.sentenceRepository.save(sentence);
            return this.convertToSentenceDto(savedSentence);
        } catch (Exception e) {
            log.error("문장 생성 실패", e);
            throw new RuntimeException("문장 생성에 실패했습니다.");
        }
    }

    public SentenceDto updateSentence(Long sentenceId, SentenceDto sentenceDto) {
        try {
            Sentence sentence = (Sentence)this.sentenceRepository.findById(sentenceId).orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));
            sentence.setEnglishText(sentenceDto.getEnglish());
            sentence.setKoreanTranslation(sentenceDto.getKorean());
            sentence.setDifficultyLevel(sentenceDto.getLevel());
            sentence.setDayNumber(sentenceDto.getDayNumber() != null ? sentenceDto.getDayNumber() : sentence.getDayNumber());
            Sentence savedSentence = (Sentence)this.sentenceRepository.save(sentence);
            return this.convertToSentenceDto(savedSentence);
        } catch (Exception e) {
            log.error("문장 수정 실패", e);
            throw new RuntimeException("문장 수정에 실패했습니다.");
        }
    }

    public void deleteSentence(Long sentenceId) {
        try {
            Sentence sentence = (Sentence)this.sentenceRepository.findById(sentenceId).orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));
            sentence.setIsActive(false);
            this.sentenceRepository.save(sentence);
        } catch (Exception e) {
            log.error("문장 삭제 실패", e);
            throw new RuntimeException("문장 삭제에 실패했습니다.");
        }
    }

    public List<SentenceDto> searchSentences(String query) {
        try {
            List<Sentence> sentences = this.sentenceRepository.searchByQuery(query);
            return (List)sentences.stream().map(this::convertToSentenceDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("문장 검색 실패", e);
            return new ArrayList();
        }
    }

    public List<SentenceDto> getSentencesByLevel(Integer level) {
        try {
            List<Sentence> sentences = this.sentenceRepository.findByDifficultyLevelAndIsActiveTrue(level);
            return (List)sentences.stream().map(this::convertToSentenceDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("레벨별 문장 조회 실패", e);
            return new ArrayList();
        }
    }

    public LearningSettingsDto getLearningSettings() {
        try {
            LearningSettings settings = (LearningSettings)this.learningSettingsRepository.findTopByOrderByCreatedAtDesc().orElse(this.getDefaultSettings());
            return this.convertToLearningSettingsDto(settings);
        } catch (Exception e) {
            log.error("학습 설정 조회 실패", e);
            return this.getDefaultLearningSettingsDto();
        }
    }

    public LearningSettingsDto saveLearningSettings(LearningSettingsDto settingsDto) {
        try {
            LearningSettings settings = LearningSettings.builder().audioSpeed(settingsDto.getAudioSpeed()).voiceSpeed(settingsDto.getVoiceSpeed()).repeatCount(settingsDto.getRepeatCount()).wordCoin(settingsDto.getWordCoin()).sentenceCoin(settingsDto.getSentenceCoin()).streakBonus(settingsDto.getStreakBonus()).levelUpCoin(settingsDto.getLevelUpCoin()).maxLevel(settingsDto.getMaxLevel()).dailyWordGoal(settingsDto.getDailyWordGoal()).dailySentenceGoal(settingsDto.getDailySentenceGoal()).build();
            LearningSettings savedSettings = (LearningSettings)this.learningSettingsRepository.save(settings);
            return this.convertToLearningSettingsDto(savedSettings);
        } catch (Exception e) {
            log.error("학습 설정 저장 실패", e);
            throw new RuntimeException("학습 설정 저장에 실패했습니다.");
        }
    }

    public LearningSettingsDto updateLearningSettings(LearningSettingsDto settingsDto) {
        return this.saveLearningSettings(settingsDto);
    }

    public String uploadWordAudio(Long wordId, MultipartFile file) {
        try {
            log.info("단어 음성 파일 S3 업로드: wordId={}, filename={}", wordId, file.getOriginalFilename());
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || 
                (!originalFilename.toLowerCase().endsWith(".mp3") && 
                 !originalFilename.toLowerCase().endsWith(".m4a") &&
                 !originalFilename.toLowerCase().endsWith(".wav") &&
                 !originalFilename.toLowerCase().endsWith(".ogg"))) {
                throw new RuntimeException("MP3, M4A, WAV, OGG 파일만 업로드 가능합니다.");
            }

            // S3에 파일 업로드
            String s3Key = s3Service.uploadFile(file, "words");

            // S3 URL 생성 (직접 URL 구성)
            String s3Url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", 
                    "kidsnreading-sounds", s3Key);

            // 파일이 실제로 업로드되었는지 확인
            if (!s3Service.fileExists(s3Key)) {
                throw new RuntimeException("S3 파일 업로드 검증 실패");
            }

            // 데이터베이스 업데이트
            Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
            word.setAudioUrl(s3Url);
            wordRepository.save(word);

            log.info("단어 음성 파일 S3 업로드 완료: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("단어 음성 파일 S3 업로드 실패: wordId={}", wordId, e);
            throw new RuntimeException("음성 파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    public String uploadSentenceAudio(Long sentenceId, MultipartFile file) {
        try {
            log.info("문장 음성 파일 S3 업로드: sentenceId={}, filename={}", sentenceId, file.getOriginalFilename());
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || 
                (!originalFilename.toLowerCase().endsWith(".mp3") && 
                 !originalFilename.toLowerCase().endsWith(".m4a") &&
                 !originalFilename.toLowerCase().endsWith(".wav") &&
                 !originalFilename.toLowerCase().endsWith(".ogg"))) {
                throw new RuntimeException("MP3, M4A, WAV, OGG 파일만 업로드 가능합니다.");
            }

            // S3에 파일 업로드
            String s3Key = s3Service.uploadFile(file, "sentences");
            String s3Url = s3Service.getS3Url(s3Key);

            // 데이터베이스 업데이트
            Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));
            sentence.setAudioUrl(s3Url);
            sentenceRepository.save(sentence);

            log.info("문장 음성 파일 S3 업로드 완료: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("문장 음성 파일 S3 업로드 실패: sentenceId={}", sentenceId, e);
            throw new RuntimeException("음성 파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteWordAudio(Long wordId) {
        try {
            log.info("단어 음성 파일 S3 삭제: wordId={}", wordId);

            Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

            if (word.getAudioUrl() != null && !word.getAudioUrl().isEmpty()) {
                // S3에서 파일 삭제 (S3 URL에서 키 추출)
                if (word.getAudioUrl().contains("amazonaws.com")) {
                    String s3Key = extractS3KeyFromUrl(word.getAudioUrl());
                    s3Service.deleteFile(s3Key);
                    log.info("단어 음성 파일 S3 삭제 완료: {}", s3Key);
                }

                // 데이터베이스에서 오디오 URL 제거
                word.setAudioUrl(null);
                wordRepository.save(word);
            }
        } catch (Exception e) {
            log.error("단어 음성 파일 S3 삭제 실패: wordId={}", wordId, e);
            throw new RuntimeException("음성 파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteSentenceAudio(Long sentenceId) {
        try {
            log.info("문장 음성 파일 S3 삭제: sentenceId={}", sentenceId);

            Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("문장을 찾을 수 없습니다."));

            if (sentence.getAudioUrl() != null && !sentence.getAudioUrl().isEmpty()) {
                // S3에서 파일 삭제 (S3 URL에서 키 추출)
                if (sentence.getAudioUrl().contains("amazonaws.com")) {
                    String s3Key = extractS3KeyFromUrl(sentence.getAudioUrl());
                    s3Service.deleteFile(s3Key);
                    log.info("문장 음성 파일 S3 삭제 완료: {}", s3Key);
                }

                // 데이터베이스에서 오디오 URL 제거
                sentence.setAudioUrl(null);
                sentenceRepository.save(sentence);
            }
        } catch (Exception e) {
            log.error("문장 음성 파일 S3 삭제 실패: sentenceId={}", sentenceId, e);
            throw new RuntimeException("음성 파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> bulkUploadWords(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        List<String> errorRows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 헤더 읽기
            if (!rowIterator.hasNext()) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("엑셀에 데이터가 없습니다."));
                return result;
            }
            Row headerRow = rowIterator.next();
            String wordHeader = headerRow.getCell(0).getStringCellValue().trim();
            String meaningHeader = headerRow.getCell(1).getStringCellValue().trim();
            String audioHeader = headerRow.getCell(2).getStringCellValue().trim();
            String levelHeader = headerRow.getCell(3) != null ? headerRow.getCell(3).getStringCellValue().trim() : null;
            String dayHeader = headerRow.getCell(4) != null ? headerRow.getCell(4).getStringCellValue().trim() : null;

            if (!"word".equalsIgnoreCase(wordHeader) ||
                !"meaning".equalsIgnoreCase(meaningHeader) ||
                !"audio_file".equalsIgnoreCase(audioHeader) ||
                (levelHeader != null && !"level".equalsIgnoreCase(levelHeader)) ||
                (dayHeader != null && !"day".equalsIgnoreCase(dayHeader))) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("헤더명이 올바르지 않습니다. (word, meaning, audio_file, level, day)"));
                return result;
            }

            // 데이터 읽기
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String word = getCellString(row.getCell(0));
                    String meaning = getCellString(row.getCell(1));
                    String audioFile = getCellString(row.getCell(2));
                    Integer level = 1;
                    Integer day = 1;
                    if (row.getCell(3) != null) {
                        String levelStr = getCellString(row.getCell(3));
                        if (levelStr != null && !levelStr.isEmpty()) {
                            try { level = (int) Double.parseDouble(levelStr); } catch (Exception ignore) {}
                        }
                    }
                    if (row.getCell(4) != null) {
                        String dayStr = getCellString(row.getCell(4));
                        if (dayStr != null && !dayStr.isEmpty()) {
                            try { day = (int) Double.parseDouble(dayStr); } catch (Exception ignore) {}
                        }
                    }

                    if (word == null || word.isEmpty() || meaning == null || meaning.isEmpty()) {
                        errorRows.add("단어 또는 의미가 비어있습니다. (행: " + (row.getRowNum() + 1) + ")");
                        errorCount++;
                        continue;
                    }

                    Word newWord = Word.builder()
                        .text(word)
                        .meaning(meaning)
                        .level(level)
                        .day(day)
                        .pronunciation(null)
                        .isActive(true)
                        .build();

                    if (audioFile != null && !audioFile.isEmpty()) {
                        // S3 URL로 저장
                        String s3Key = s3Service.buildS3Key("words", audioFile);
                        String s3Url = s3Service.getS3Url(s3Key);
                        newWord.setAudioUrl(s3Url);
                    } else {
                        newWord.setAudioUrl(null);
                    }
                    this.wordRepository.save(newWord);
                    successCount++;
                } catch (Exception e) {
                    errorRows.add("행 " + (row.getRowNum() + 1) + " 오류: " + e.getMessage());
                    errorCount++;
                }
            }
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errorRows", errorRows);
        } catch (Exception e) {
            log.error("단어 일괄 업로드 실패", e);
            result.put("successCount", 0);
            result.put("errorCount", 1);
            ((List<String>) result.get("errorRows")).add("파일 읽기 중 오류 발생: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> bulkUploadSentences(MultipartFile file) {
        log.info("문장 일괄 업로드: filename={}", file.getOriginalFilename());
        Map<String, Object> result = new HashMap();
        int successCount = 0;
        int errorCount = 0;
        List<String> errorRows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 헤더 읽기
            if (!rowIterator.hasNext()) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("엑셀에 데이터가 없습니다."));
                return result;
            }
            Row headerRow = rowIterator.next();
            String englishHeader = headerRow.getCell(0).getStringCellValue().trim();
            String koreanHeader = headerRow.getCell(1).getStringCellValue().trim();
            String audioHeader = headerRow.getCell(2).getStringCellValue().trim();
            String levelHeader = headerRow.getCell(3) != null ? headerRow.getCell(3).getStringCellValue().trim() : null;
            String dayHeader = headerRow.getCell(4) != null ? headerRow.getCell(4).getStringCellValue().trim() : null;

            if (!"english".equalsIgnoreCase(englishHeader) ||
                !"korean".equalsIgnoreCase(koreanHeader) ||
                !"audio_file".equalsIgnoreCase(audioHeader) ||
                (levelHeader != null && !"level".equalsIgnoreCase(levelHeader)) ||
                (dayHeader != null && !"day".equalsIgnoreCase(dayHeader))) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("헤더명이 올바르지 않습니다. (english, korean, audio_file, level, day)"));
                return result;
            }

            // 데이터 읽기
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String english = getCellString(row.getCell(0));
                    String korean = getCellString(row.getCell(1));
                    String audioFile = getCellString(row.getCell(2));
                    Integer level = 1;
                    Integer day = 1;
                    if (row.getCell(3) != null) {
                        String levelStr = getCellString(row.getCell(3));
                        if (levelStr != null && !levelStr.isEmpty()) {
                            try { level = (int) Double.parseDouble(levelStr); } catch (Exception ignore) {}
                        }
                    }
                    if (row.getCell(4) != null) {
                        String dayStr = getCellString(row.getCell(4));
                        if (dayStr != null && !dayStr.isEmpty()) {
                            try { day = (int) Double.parseDouble(dayStr); } catch (Exception ignore) {}
                        }
                    }

                    if (english == null || english.isEmpty() || korean == null || korean.isEmpty()) {
                         errorRows.add("영어 또는 한국어가 비어있습니다. (행: " + (row.getRowNum() + 1) + ")");
                         errorCount++;
                         continue;
                    }

                    Sentence newSentence = Sentence.builder()
                        .englishText(english)
                        .koreanTranslation(korean)
                        .difficultyLevel(level)
                        .dayNumber(day)
                        .isActive(true)
                        .build();

                    if (audioFile != null && !audioFile.isEmpty()) {
                        // S3 URL로 저장
                        String s3Key = s3Service.buildS3Key("sentences", audioFile);
                        String s3Url = s3Service.getS3Url(s3Key);
                        newSentence.setAudioUrl(s3Url);
                    } else {
                        newSentence.setAudioUrl(null);
                    }
                    this.sentenceRepository.save(newSentence);
                    successCount++;
                } catch (Exception e) {
                    errorRows.add("행 " + (row.getRowNum() + 1) + " 오류: " + e.getMessage());
                    errorCount++;
                }
            }
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errorRows", errorRows);
        } catch (Exception e) {
            log.error("문장 일괄 업로드 실패", e);
            result.put("successCount", 0);
            result.put("errorCount", 1);
            result.put("errorRows", List.of("파일 읽기 중 오류 발생: " + e.getMessage()));
        }

        return result;
    }

    public Map<String, Object> bulkUploadWordAudio(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0, errorCount = 0;
        List<String> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    try {
                        // 임시 파일로 저장
                        File tempFile = File.createTempFile("audio_", "_" + fileName);
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        // S3 업로드
                        String s3Key = s3Service.uploadFileWithOriginalName(tempFile, "words", fileName);
                        String s3Url = s3Service.getS3Url(s3Key);
                        // DB audioUrl 업데이트 (파일명 매칭)
                        List<Word> words = wordRepository.findByIsActiveTrue();
                        for (Word word : words) {
                            if (word.getAudioUrl() != null && (word.getAudioUrl().endsWith("/" + fileName) || word.getAudioUrl().equals(fileName))) {
                                word.setAudioUrl(s3Url);
                                wordRepository.save(word);
                            }
                        }
                        successFiles.add(fileName);
                        successCount++;
                        tempFile.delete();
                    } catch (Exception e) {
                        errorFiles.add(fileName + ": " + e.getMessage());
                        errorCount++;
                    }
                }
            }
        } catch (Exception e) {
            errorFiles.add("ZIP 해제 실패: " + e.getMessage());
            errorCount++;
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("successFiles", successFiles);
        result.put("errorFiles", errorFiles);
        return result;
    }

    public Map<String, Object> bulkUploadSentenceAudio(MultipartFile file) {
        log.info("문장 음원 일괄 업로드: filename={}", file.getOriginalFilename());
        Map<String, Object> result = new HashMap<>();
        int successCount = 0, errorCount = 0;
        List<String> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    try {
                        // 임시 파일로 저장
                        File tempFile = File.createTempFile("audio_", null);
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        // S3 업로드 (원래 파일명으로)
                        s3Service.uploadFileWithOriginalName(tempFile, "sentences", fileName);
                        successFiles.add(fileName);
                        successCount++;
                        tempFile.delete();
                    } catch (Exception e) {
                        errorFiles.add(fileName + ": " + e.getMessage());
                        errorCount++;
                    }
                }
            }
        } catch (Exception e) {
            errorFiles.add("ZIP 해제 실패: " + e.getMessage());
            errorCount++;
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("successFiles", successFiles);
        result.put("errorFiles", errorFiles);
        return result;
    }

    public List<Map<String, Object>> getUserProgress() {
        return new ArrayList();
    }

    public List<Map<String, Object>> getWordStats() {
        return new ArrayList();
    }

    public Map<String, Object> getDailyStats(String date) {
        return new HashMap();
    }

    private UserDto convertToUserDto(User user) {
        return UserDto.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).name(user.getName()).role(user.getRole().name()).isActive(user.getIsActive()).build();
    }

    private WordDto convertToWordDto(Word word) {
        return WordDto.builder().id(word.getId()).english(word.getText()).korean(word.getMeaning()).level(word.getLevel()).day(word.getDay()).pronunciation(word.getPronunciation()).audioUrl(word.getAudioUrl()).isActive(word.getIsActive()).build();
    }

    private SentenceDto convertToSentenceDto(Sentence sentence) {
        return SentenceDto.builder()
                .id(sentence.getId())
                .text(sentence.getEnglishText())
                .sentence(sentence.getEnglishText())
                .english(sentence.getEnglishText())
                .korean(sentence.getKoreanTranslation())
                .translation(sentence.getKoreanTranslation())
                .level(sentence.getDifficultyLevel())
                .dayNumber(sentence.getDayNumber())
                .audioUrl(sentence.getAudioUrl())
                .isActive(sentence.getIsActive())
                .createdAt(sentence.getCreatedAt())
                .updatedAt(sentence.getUpdatedAt())
                .phonetic(sentence.getPhonetic())
                .pronunciation(sentence.getPronunciation())
                .build();
    }

    private LearningSettingsDto convertToLearningSettingsDto(LearningSettings settings) {
        return LearningSettingsDto.builder().audioSpeed(settings.getAudioSpeed()).voiceSpeed(settings.getVoiceSpeed()).repeatCount(settings.getRepeatCount()).wordCoin(settings.getWordCoin()).sentenceCoin(settings.getSentenceCoin()).streakBonus(settings.getStreakBonus()).levelUpCoin(settings.getLevelUpCoin()).maxLevel(settings.getMaxLevel()).dailyWordGoal(settings.getDailyWordGoal()).dailySentenceGoal(settings.getDailySentenceGoal()).build();
    }

    private LearningSettings getDefaultSettings() {
        return LearningSettings.builder().audioSpeed(0.8).voiceSpeed(1).repeatCount(3).wordCoin(1).sentenceCoin(3).streakBonus(5).levelUpCoin(100).maxLevel(10).dailyWordGoal(10).dailySentenceGoal(5).build();
    }

    private LearningSettingsDto getDefaultLearningSettingsDto() {
        return LearningSettingsDto.builder().audioSpeed(0.8).voiceSpeed(1).repeatCount(3).wordCoin(1).sentenceCoin(3).streakBonus(5).levelUpCoin(100).maxLevel(10).dailyWordGoal(10).dailySentenceGoal(5).build();
    }

    private String getCellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    // === 파일명 정규화 함수 추가 ===
    private String normalizeAudioFileName(String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String base = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("문장\\s*(\\d+)번").matcher(base);
        if (m.find()) return "sentence" + m.group(1) + ext;
        m = java.util.regex.Pattern.compile("(\\d+)번").matcher(base);
        if (m.find()) return "no" + m.group(1) + ext;
        return originalFilename;
    }

    private String extractS3KeyFromUrl(String s3Url) {
        // S3 URL에서 키 추출: https://bucket.s3.region.amazonaws.com/key
        String[] parts = s3Url.split("amazonaws.com/");
        if (parts.length > 1) {
            return parts[1];
        }
        return s3Url; // URL이 아닌 경우 그대로 반환
    }

    // === 기존 DB 데이터 정규화 메서드 추가 ===
    @Transactional
    public void normalizeExistingAudioFileNames() {
        log.info("기존 오디오 파일명 정규화 시작");

        // 단어 오디오 파일명 정규화
        List<Word> words = wordRepository.findAll();
        int wordUpdateCount = 0;
        for (Word word : words) {
            if (word.getAudioUrl() != null && !word.getAudioUrl().isEmpty()) {
                String originalUrl = word.getAudioUrl();
                String fileName = originalUrl.substring(originalUrl.lastIndexOf("/") + 1);
                String normalizedFileName = normalizeAudioFileName(fileName);
                String normalizedUrl = "/audio/words/" + normalizedFileName;

                if (!originalUrl.equals(normalizedUrl)) {
                    word.setAudioUrl(normalizedUrl);
                    wordRepository.save(word);
                    wordUpdateCount++;
                    log.info("단어 오디오 파일명 정규화: {} -> {}", originalUrl, normalizedUrl);
                }
            }
        }
        log.info("단어 오디오 파일명 정규화 완료: {}개 업데이트", wordUpdateCount);

        // 문장 오디오 URL 정규화
        List<Sentence> sentences = sentenceRepository.findAll();
        int sentenceUpdateCount = 0;

        for (Sentence sentence : sentences) {
            if (sentence.getAudioUrl() != null && !sentence.getAudioUrl().isEmpty()) {
                String originalUrl = sentence.getAudioUrl();
                // S3 URL에서 공백이 포함된 파일명을 언더스코어로 변경
                if (originalUrl.contains(" ")) {
                    String[] parts = originalUrl.split("/");
                    if (parts.length > 0) {
                        String fileName = parts[parts.length - 1];
                        String encodedFileName = fileName.replaceAll("\\s+", "_");
                        String newUrl = originalUrl.replace(fileName, encodedFileName);
                        sentence.setAudioUrl(newUrl);
                        sentenceRepository.save(sentence);
                        sentenceUpdateCount++;
                        log.info("문장 오디오 URL 수정: {} -> {}", originalUrl, newUrl);
                    }
                }
            }
        }

        log.info("오디오 파일명 정규화 완료: 단어 {}개, 문장 {}개 업데이트", wordUpdateCount, sentenceUpdateCount);
    }

    @Generated
    public AdminService(final UserRepository userRepository, final WordRepository wordRepository, final SentenceRepository sentenceRepository, final LearningSettingsRepository learningSettingsRepository, final UserWordProgressRepository userWordProgressRepository, final UserSentenceProgressRepository userSentenceProgressRepository, final S3Service s3Service) {
        this.userRepository = userRepository;
        this.wordRepository = wordRepository;
        this.sentenceRepository = sentenceRepository;
        this.learningSettingsRepository = learningSettingsRepository;
        this.userWordProgressRepository = userWordProgressRepository;
        this.userSentenceProgressRepository = userSentenceProgressRepository;
        this.s3Service = s3Service;
    }
}