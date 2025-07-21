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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

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
    @Autowired
    private PasswordEncoder passwordEncoder;

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
                User user = User.builder()
                    .username(userDto.getUsername())
                    .email(userDto.getEmail())
                    .name(userDto.getName())
                    .password(passwordEncoder.encode(userDto.getPassword()))
                    .role(Role.valueOf(userDto.getRole()))
                    .groupName(userDto.getGroupName()) // 추가
                    .isActive(userDto.getIsActive() != null ? userDto.getIsActive() : true)
                    .build();
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
            user.setRole(Role.valueOf(userDto.getRole())); // 역할도 수정 가능하게 다시 활성화
            user.setGroupName(userDto.getGroupName()); // 추가
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            if (userDto.getIsActive() != null) {
                user.setIsActive(userDto.getIsActive());
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
            // ID 오름차순 정렬 (엑셀 업로드 순서)
            words.sort(Comparator.comparing(Word::getId));
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
            // ID 오름차순 정렬 (엑셀 업로드 순서)
            sentences.sort(Comparator.comparing(Sentence::getId));
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
            int displayOrder = 1;
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
                        .displayOrder(displayOrder)
                        .build();

                    // audio_file 컬럼 데이터는 무시하고 항상 폴더 경로만 저장 (word/ 접두사 추가)
                    String audioUrl = String.format("word/level%d/day%d/", level, day);
                    if (newWord.getAudioUrl() == null || newWord.getAudioUrl().isEmpty() || !newWord.getAudioUrl().startsWith("http")) {
                        newWord.setAudioUrl(audioUrl);
                    }
                    this.wordRepository.save(newWord);
                    displayOrder++;
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
            String sentenceHeader = headerRow.getCell(0).getStringCellValue().trim();
            String meaningHeader = headerRow.getCell(1).getStringCellValue().trim();
            String audioHeader = headerRow.getCell(2).getStringCellValue().trim();
            String levelHeader = headerRow.getCell(3) != null ? headerRow.getCell(3).getStringCellValue().trim() : null;
            String dayHeader = headerRow.getCell(4) != null ? headerRow.getCell(4).getStringCellValue().trim() : null;

            if (!"sentence".equalsIgnoreCase(sentenceHeader) ||
                !"meaning".equalsIgnoreCase(meaningHeader) ||
                !"audio_file".equalsIgnoreCase(audioHeader) ||
                (levelHeader != null && !"level".equalsIgnoreCase(levelHeader)) ||
                (dayHeader != null && !"day".equalsIgnoreCase(dayHeader))) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errorRows", List.of("헤더명이 올바르지 않습니다. (sentence, meaning, audio_file, level, day)"));
                return result;
            }

            int sentenceCounter = 1; // 문장 ID 카운터
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String sentence = getCellString(row.getCell(0));
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

                    if (sentence == null || sentence.isEmpty() || meaning == null || meaning.isEmpty()) {
                         errorRows.add("문장 또는 의미가 비어있습니다. (행: " + (row.getRowNum() + 1) + ")");
                         errorCount++;
                         continue;
                    }

                    Sentence newSentence = Sentence.builder()
                        .englishText(sentence)
                        .koreanTranslation(meaning)
                        .difficultyLevel(level)
                        .dayNumber(day)
                        .isActive(true)
                        .build();

                    // audio_file 컬럼 데이터는 무시하고 항상 자동으로 고유 파일명 생성 (sentence/ 접두사 추가)
                    String generatedAudioFile = String.format("sentence/level%d/day%d/level%d_day%d_sentence%d", level, day, level, day, sentenceCounter);
                    sentenceCounter++;

                    // 항상 전체 S3 경로로 맞춰줌 (저장 직전)
                    String finalAudioFile = String.format(
                        "vocabulary/%s/sentences/%s",
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM-dd")),
                        generatedAudioFile
                    );
                    if (newSentence.getAudioUrl() == null || newSentence.getAudioUrl().isEmpty() || !newSentence.getAudioUrl().startsWith("http")) {
                        newSentence.setAudioUrl(finalAudioFile);
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
        int successCount =0, errorCount = 0;
        List<String> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        //1. ZIP 내 모든 파일을 한 번에 읽어서 메모리에 저장
        Map<String, byte[]> fileContents = new HashMap<>();
        Map<String, List<String>> folderFiles = new HashMap<>();
        List<ZipEntry> entries = new ArrayList<>();
        boolean zipParsed = false;
        Exception lastException = null;
        Set<String> processedFiles = new HashSet<>(); // 이미 처리된 파일 추적
        Set<Long> processedWordIds = new HashSet<>(); // 이미 처리된 문장 ID 추적
        
        for (java.nio.charset.Charset charset : new java.nio.charset.Charset[]{java.nio.charset.StandardCharsets.UTF_8, java.nio.charset.Charset.forName("CP437")}) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream(), charset)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        String filePath = entry.getName().replace("\\", "/");
                        
                        // 중복 파일 체크 - 같은 경로의 파일이 이미 있으면 건너뛰기
                        if (fileContents.containsKey(filePath)) {
                            log.warn("중복 파일 발견, 건너뛰기: {}", filePath);
                            continue;
                        }
                        
                        // 파일 내용을 메모리에 저장
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        fileContents.put(filePath, baos.toByteArray());
                        
                        // 폴더별로 파일 분류
                        int lastSlash = filePath.lastIndexOf('/');
                        String folder = (lastSlash > 0) ? filePath.substring(0, lastSlash + 1) : "";
                        folderFiles.computeIfAbsent(folder, k -> new ArrayList<>()).add(filePath);
                        
                        log.info("ZIP 파일 읽기: {} -> 폴더: {}", filePath, folder);
                    }
                }
                zipParsed = true;
                break;
            } catch (Exception e) {
                lastException = e;
                // Try next charset
            }
        }
        
        log.info("ZIP 파일 읽기 완료 - 총 파일 수: {}, 폴더 수: {}", fileContents.size(), folderFiles.size());
        for (String folder : folderFiles.keySet()) {
            log.info("폴더 {}: {}개 파일", folder, folderFiles.get(folder).size());
        }

        // 2. 폴더별로 파일/단어 매칭 (순서 보장)
        
        for (String folder : folderFiles.keySet()) {
            List<String> fileList = folderFiles.get(folder);
            log.info("폴더 {} 처리 시작 - 파일 개수:{}", folder, fileList.size());
            
            // 파일명 기준 숫자 순서대로 정렬 (1, ..., 10, 11, ...)
            Collections.sort(fileList, (a, b) -> {
                String aName = a.substring(a.lastIndexOf('/') + 1);
                String bName = b.substring(b.lastIndexOf('/') + 1);
                
                // 파일명에서 숫자 부분 추출
                String aNumber = extractNumberFromFileName(aName);
                String bNumber = extractNumberFromFileName(bName);
                
                // 숫자가 있으면 숫자로 비교, 없으면 문자열 비교
                if (aNumber != null && bNumber != null) {
                    try {
                        int aNum = Integer.parseInt(aNumber);
                        int bNum = Integer.parseInt(bNumber);
                        return Integer.compare(aNum, bNum);
                    } catch (NumberFormatException e) {
                        return aName.compareTo(bName);
                    }
                } else {
                    return aName.compareTo(bName);
                }
            });
            
            log.info("폴더 {} 파일 정렬 후:{}", folder, fileList);
            
            // audioUrl이 해당 폴더로 시작하는 단어만 추출하고 displayOrder 오름차순 정렬(엑셀 업로드 순서)
            List<Word> wordList = wordRepository.findByAudioUrlStartingWith(folder);
            wordList.sort(Comparator.comparing(Word::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)));
            
            log.info("폴더 {} 매칭된 단어 개수: {}", folder, wordList.size());
            for (Word word : wordList) {
                log.info("단어 ID: {} 텍스트: {} udioUrl: {}", word.getId(), word.getText(), word.getAudioUrl());
            }
            
            int matchCount = Math.min(fileList.size(), wordList.size());
            log.info("폴더 {} 최종 매칭 개수:{}개 (파일: {}개, 단어: {}개)", folder, matchCount, fileList.size(), wordList.size());
            
            // 메모리에서 파일을 추출하여 S3 업로드
            for (int i = 0; i < matchCount; i++) {
                String filePath = fileList.get(i);
                Word matchingWord = wordList.get(i);
                
                // 이미 처리된 파일이나 단어인지 확인
                if (processedFiles.contains(filePath)) {
                    log.warn("이미 처리된 파일 건너뛰기: {}", filePath);
                    continue;
                }
                if (processedWordIds.contains(matchingWord.getId())) {
                    log.warn("이미 처리된 단어 건너뛰기: ID={}, 텍스트={}", matchingWord.getId(), matchingWord.getText());
                    continue;
                }
                
                log.info("매칭 {}: 파일 {} -> 단어{} (ID: {})", i+1, filePath, matchingWord.getText(), matchingWord.getId());
                
                try {
                    // 메모리에서 파일 내용 가져오기
                    byte[] fileContent = fileContents.get(filePath);
                    if (fileContent == null) {
                        errorCount++;
                        errorFiles.add(filePath + ":파일 내용을 찾을 수 없음");
                        log.error("실패: 파일 {} 내용을 찾을 수 없음", filePath);
                        continue;
                    }
                    
                    // 임시 파일 생성
                    File tempFile = File.createTempFile("audio_", null);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(fileContent);
                    }
                    
                    // S3 업로드 (word/ 접두사 추가)
                    String s3Key = "word/" + filePath;
                    String uploadedKey = s3Service.uploadFileWithKey(tempFile, s3Key);
                    String s3Url = s3Service.getS3Url(uploadedKey);
                    
                    // audioUrl에 S3 URL 저장
                    matchingWord.setAudioUrl(s3Url);
                    wordRepository.save(matchingWord);
                    
                    tempFile.delete();
                    successCount++;
                    successFiles.add(filePath);
                    
                    // 처리 완료 표시
                    processedFiles.add(filePath);
                    processedWordIds.add(matchingWord.getId());
                    
                    log.info("성공: 파일 {} -> S3 {} -> 단어 {}", filePath, s3Key, matchingWord.getText());
                    
                } catch (Exception e) {
                    errorCount++;
                    errorFiles.add(filePath + ": " + e.getMessage());
                    log.error("실패:파일 {} 처리 실패", filePath, e);
                }
            }
        }
        
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("successFiles", successFiles);
        result.put("errorFiles", errorFiles);
        return result;
    }

    @Transactional
    public Map<String, Object> bulkUploadSentenceAudio(MultipartFile file) {
        log.info("문장 음원 일괄 업로드: filename={}", file.getOriginalFilename());
        Map<String, Object> result = new HashMap<>();
        int successCount = 0, errorCount = 0;
        List<String> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        //1. ZIP 내 모든 파일을 한 번에 읽어서 메모리에 저장
        Map<String, byte[]> fileContents = new HashMap<>();
        Map<String, List<String>> folderFiles = new HashMap<>();
        boolean zipParsed = false;
        Exception lastException = null;
        Set<String> processedFiles = new HashSet<>(); // 이미 처리된 파일 추적
        Set<Long> processedWordIds = new HashSet<>(); // 이미 처리된 문장 ID 추적

        for (java.nio.charset.Charset charset : new java.nio.charset.Charset[]{java.nio.charset.StandardCharsets.UTF_8, java.nio.charset.Charset.forName("CP437")}) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream(), charset)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        String filePath = entry.getName().replace("\\", "/");
                        // 파일 내용을 메모리에 저장
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        fileContents.put(filePath, baos.toByteArray());
                        // 폴더별로 파일 분류
                        int lastSlash = filePath.lastIndexOf('/');
                        String folder = (lastSlash > 0) ? filePath.substring(0, lastSlash + 1) : "";
                        folderFiles.computeIfAbsent(folder, k -> new ArrayList<>()).add(filePath);
                    }
                }
                zipParsed = true;
                break;
            } catch (Exception e) {
                lastException = e;
            }
        }
        if (!zipParsed) {
            log.error("ZIP 파일 처리 중 오류 발생 (모든 인코딩 실패)", lastException);
            errorFiles.add("ZIP 해제 실패: " + (lastException != null ? lastException.getMessage() : "알 수 없는 오류"));
            errorCount++;
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("successFiles", successFiles);
            result.put("errorFiles", errorFiles);
            return result;
        }
        // 폴더별로 파일/문장 매칭 (조건: 레벨, 데이, 활성)
        for (String folder : folderFiles.keySet()) {
            List<String> fileList = folderFiles.get(folder);
            Collections.sort(fileList);
            // 폴더명에서 레벨, 데이 추출 (예: sentence/level1/day1/)
            int level = 1, day = 1;
            try {
                String[] parts = folder.replace("sentence/level", "").replace("/day", "/").split("/");
                if (parts.length >= 2) {
                    level = Integer.parseInt(parts[0]);
                    day = Integer.parseInt(parts[1]);
                }
            } catch (Exception ignore) {}
            // 해당 레벨/데이/활성 문장만 추출
            List<Sentence> sentenceList = sentenceRepository.findByDifficultyLevelAndDayNumberAndIsActiveTrue(level, day);
            sentenceList.sort(Comparator.comparing(Sentence::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)));
            int matchCount = Math.min(fileList.size(), sentenceList.size());
            for (int i = 0; i < matchCount; i++) {
                String filePath = fileList.get(i);
                Sentence matchingSentence = sentenceList.get(i);
                
                // 이미 처리된 파일이나 문장인지 확인
                if (processedFiles.contains(filePath)) {
                    log.warn("이미 처리된 파일 건너뛰기: {}", filePath);
                    continue;
                }
                if (processedWordIds.contains(matchingSentence.getId())) { // Sentence ID를 사용
                    log.warn("이미 처리된 문장 건너뛰기: ID={}, 텍스트={}", matchingSentence.getId(), matchingSentence.getEnglishText());
                    continue;
                }
                
                log.info("매칭 {}: 파일 {} -> 문장{} (ID: {})", i+1, filePath, matchingSentence.getEnglishText(), matchingSentence.getId());
                
                try {
                    // 메모리에서 파일 내용 가져오기
                    byte[] fileContent = fileContents.get(filePath);
                    if (fileContent == null) {
                        errorCount++;
                        errorFiles.add(filePath + ":파일 내용을 찾을 수 없음");
                        log.error("실패: 파일 {} 내용을 찾을 수 없음", filePath);
                        continue;
                    }
                    
                    // 임시 파일 생성
                    File tempFile = File.createTempFile("audio_", null);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(fileContent);
                    }
                    
                    // S3 업로드 (sentence/ 접두사 추가)
                    String s3Key = "sentence/" + filePath;
                    String uploadedKey = s3Service.uploadFileWithKey(tempFile, s3Key);
                    String s3Url = s3Service.getS3Url(uploadedKey);
                    
                    // audioUrl에 S3 URL 저장
                    matchingSentence.setAudioUrl(s3Url);
                    sentenceRepository.save(matchingSentence);
                    
                    tempFile.delete();
                    successCount++;
                    successFiles.add(filePath);
                    
                    // 처리 완료 표시
                    processedFiles.add(filePath);
                    processedWordIds.add(matchingSentence.getId()); // Sentence ID를 사용
                    
                    log.info("성공: 파일 {} -> S3 {} -> 문장 {}", filePath, s3Key, matchingSentence.getEnglishText());
                    
                } catch (Exception e) {
                    errorCount++;
                    errorFiles.add(filePath + ": " + e.getMessage());
                    log.error("실패:파일 {} 처리 실패", filePath, e);
                }
            }
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
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole().name())
            .groupName(user.getGroupName())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLogin(user.getLastLogin())
            .parentName(user.getParentName())
            .phoneNumber(user.getPhoneNumber())
            .build();
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

    // 파일명에서 숫자 부분을 추출하는 헬퍼 메서드
    private String extractNumberFromFileName(String fileName) {
        // 확장자 제거
        String nameWithoutExt = fileName;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            nameWithoutExt = fileName.substring(0, dotIndex);
        }
        
        // 파일명 시작 부분에서 연속된 숫자 찾기
        StringBuilder number = new StringBuilder();
        for (char c : nameWithoutExt.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (number.length() > 0) {
                // 숫자가 시작되었는데 다른 문자가 나오면 중단
                break;
            }
        }
        
        return number.length() > 0 ? number.toString() : null;
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