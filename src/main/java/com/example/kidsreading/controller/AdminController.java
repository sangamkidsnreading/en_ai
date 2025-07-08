//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.kidsreading.controller;

import com.example.kidsreading.dto.AdminStatsDto;
import com.example.kidsreading.dto.LearningSettingsDto;
import com.example.kidsreading.dto.SentenceDto;
import com.example.kidsreading.dto.UserDto;
import com.example.kidsreading.dto.WordDto;
import com.example.kidsreading.service.AdminService;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.kidsreading.entity.Word;
import com.example.kidsreading.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.kidsreading.entity.Sentence;
import com.example.kidsreading.repository.SentenceRepository;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import com.example.kidsreading.service.S3Service;
import java.util.ArrayList;
import com.example.kidsreading.service.SentenceService;

@RestController
@RequestMapping({"/api/admin"})
public class AdminController {
    private final AdminService adminService;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private SentenceRepository sentenceRepository;
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private S3Service s3Service;

    @Autowired
    private SentenceService sentenceService;

    @GetMapping({"/stats"})
    public ResponseEntity<AdminStatsDto> getAdminStats() {
        AdminStatsDto stats = this.adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping({"/users"})
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = this.adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping({"/users"})
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = this.adminService.createUser(userDto);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping({"/users/{userId}"})
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        UserDto updatedUser = this.adminService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping({"/users/{userId}"})
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        this.adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "사용자가 성공적으로 삭제되었습니다."));
    }

    @GetMapping({"/users/search"})
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<UserDto> users = this.adminService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping({"/words"})
    public ResponseEntity<List<WordDto>> getAllWords() {
        List<WordDto> words = this.adminService.getAllWords();
        return ResponseEntity.ok(words);
    }

    @PostMapping({"/words"})
    public ResponseEntity<WordDto> createWord(@RequestBody WordDto wordDto) {
        WordDto createdWord = this.adminService.createWord(wordDto);
        return ResponseEntity.ok(createdWord);
    }

    @PutMapping({"/words/{wordId}"})
    public ResponseEntity<WordDto> updateWord(@PathVariable Long wordId, @RequestBody WordDto wordDto) {
        WordDto updatedWord = this.adminService.updateWord(wordId, wordDto);
        return ResponseEntity.ok(updatedWord);
    }

    @DeleteMapping({"/words/{wordId}"})
    public ResponseEntity<Map<String, String>> deleteWord(@PathVariable Long wordId) {
        this.adminService.deleteWord(wordId);
        return ResponseEntity.ok(Map.of("message", "단어가 성공적으로 삭제되었습니다."));
    }

    @GetMapping({"/words/search"})
    public ResponseEntity<List<WordDto>> searchWords(@RequestParam String query) {
        List<WordDto> words = this.adminService.searchWords(query);
        return ResponseEntity.ok(words);
    }

    @GetMapping({"/words/level/{level}"})
    public ResponseEntity<List<WordDto>> getWordsByLevel(@PathVariable Integer level) {
        List<WordDto> words = this.adminService.getWordsByLevel(level);
        return ResponseEntity.ok(words);
    }

    @PostMapping({"/words/{wordId}/audio"})
    public ResponseEntity<Map<String, String>> uploadWordAudio(@PathVariable Long wordId, @RequestParam("file") MultipartFile file) {
        String audioUrl = this.adminService.uploadWordAudio(wordId, file);
        return ResponseEntity.ok(Map.of("audioUrl", audioUrl));
    }

    @DeleteMapping({"/words/{wordId}/audio"})
    public ResponseEntity<Map<String, String>> deleteWordAudio(@PathVariable Long wordId) {
        this.adminService.deleteWordAudio(wordId);
        return ResponseEntity.ok(Map.of("message", "단어 음원이 삭제되었습니다."));
    }

    @GetMapping({"/sentences"})
    public ResponseEntity<List<SentenceDto>> getAllSentences() {
        List<SentenceDto> sentences = this.adminService.getAllSentences();
        return ResponseEntity.ok(sentences);
    }

    @PostMapping({"/sentences"})
    public ResponseEntity<SentenceDto> createSentence(@RequestBody SentenceDto sentenceDto) {
        SentenceDto createdSentence = this.adminService.createSentence(sentenceDto);
        return ResponseEntity.ok(createdSentence);
    }

    @PutMapping({"/sentences/{sentenceId}"})
    public ResponseEntity<SentenceDto> updateSentence(@PathVariable Long sentenceId, @RequestBody SentenceDto sentenceDto) {
        SentenceDto updatedSentence = this.adminService.updateSentence(sentenceId, sentenceDto);
        return ResponseEntity.ok(updatedSentence);
    }

    @DeleteMapping({"/sentences/{sentenceId}"})
    public ResponseEntity<Map<String, String>> deleteSentence(@PathVariable Long sentenceId) {
        this.adminService.deleteSentence(sentenceId);
        return ResponseEntity.ok(Map.of("message", "문장이 성공적으로 삭제되었습니다."));
    }

    @GetMapping({"/sentences/search"})
    public ResponseEntity<List<SentenceDto>> searchSentences(@RequestParam String query) {
        List<SentenceDto> sentences = this.adminService.searchSentences(query);
        return ResponseEntity.ok(sentences);
    }

    @GetMapping({"/sentences/level/{level}"})
    public ResponseEntity<List<SentenceDto>> getSentencesByLevel(@PathVariable Integer level) {
        List<SentenceDto> sentences = this.adminService.getSentencesByLevel(level);
        return ResponseEntity.ok(sentences);
    }

    @PostMapping({"/sentences/{sentenceId}/audio"})
    public ResponseEntity<Map<String, String>> uploadSentenceAudio(@PathVariable Long sentenceId, @RequestParam("file") MultipartFile file) {
        String audioUrl = this.adminService.uploadSentenceAudio(sentenceId, file);
        return ResponseEntity.ok(Map.of("audioUrl", audioUrl));
    }

    @DeleteMapping({"/sentences/{sentenceId}/audio"})
    public ResponseEntity<Map<String, String>> deleteSentenceAudio(@PathVariable Long sentenceId) {
        this.adminService.deleteSentenceAudio(sentenceId);
        return ResponseEntity.ok(Map.of("message", "문장 음원이 삭제되었습니다."));
    }

    @GetMapping({"/settings"})
    public ResponseEntity<LearningSettingsDto> getLearningSettings() {
        LearningSettingsDto settings = this.adminService.getLearningSettings();
        return ResponseEntity.ok(settings);
    }

    @PostMapping({"/settings"})
    public ResponseEntity<LearningSettingsDto> saveLearningSettings(@RequestBody LearningSettingsDto settingsDto) {
        LearningSettingsDto savedSettings = this.adminService.saveLearningSettings(settingsDto);
        return ResponseEntity.ok(savedSettings);
    }

    @PostMapping({"/words/bulk-upload"})
    public ResponseEntity<Map<String, Object>> bulkUploadWords(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = this.adminService.bulkUploadWords(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sentences/bulk-upload")
    public ResponseEntity<Map<String, Object>> bulkUploadSentences(@RequestParam("file") MultipartFile file) {
        log.info("문장 엑셀 업로드 요청 들어옴: {}", file.getOriginalFilename());
        Map<String, Object> result = new HashMap<>();
        try {
            // 엑셀 파싱 및 저장 로직
            int count = sentenceService.bulkUploadFromExcel(file);
            result.put("success", true);
            result.put("successCount", count);
            result.put("errorCount", 0);
            result.put("message", count + "개의 문장이 업로드되었습니다.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("문장 엑셀 업로드 실패", e);
            result.put("success", false);
            result.put("message", "업로드 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping({"/words/bulk-audio-upload"})
    public ResponseEntity<Map<String, Object>> bulkUploadWordAudio(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = this.adminService.bulkUploadWordAudio(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping({"/sentences/bulk-audio-upload"})
    public ResponseEntity<Map<String, Object>> bulkUploadSentenceAudio(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = this.adminService.bulkUploadSentenceAudio(file);
        return ResponseEntity.ok(result);
    }

    // 오디오 파일 서빙 엔드포인트 추가
    @GetMapping({"/audio/words/{wordId}"})
    public ResponseEntity<?> getWordAudio(@PathVariable Long wordId) {
        try {
            // DB에서 audioUrl을 가져옴
            Word word = wordRepository.findById(wordId)
                .orElse(null);
            String audioUrl = (word != null) ? word.getAudioUrl() : null;
            if (audioUrl != null && !audioUrl.isEmpty()) {
                if (audioUrl.startsWith("http")) {
                    // S3 URL이면 리다이렉트
                    return ResponseEntity.status(302).header("Location", audioUrl).build();
                }
                String filePath = System.getProperty("user.dir") + audioUrl;
                java.io.File audioFile = new java.io.File(filePath);
                if (audioFile.exists()) {
                    String contentType = null;
                    String lower = audioUrl.toLowerCase();
                    if (lower.endsWith(".mp3")) contentType = "audio/mpeg";
                    else if (lower.endsWith(".m4a")) contentType = "audio/mp4";
                    else if (lower.endsWith(".wav")) contentType = "audio/wav";
                    else if (lower.endsWith(".ogg")) contentType = "audio/ogg";
                    else contentType = "application/octet-stream";
                    org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(audioFile);
                    return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(resource);
                }
            }
            // 기존 방식: 확장자별로 직접 확인 (백워드 호환)
            String[] exts = {".mp3", ".m4a", ".wav", ".ogg"};
            for (String ext : exts) {
                String path = System.getProperty("user.dir") + "/audio/words/" + wordId + ext;
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    String contentType =
                        ext.equals(".mp3") ? "audio/mpeg" :
                        ext.equals(".m4a") ? "audio/mp4" :
                        ext.equals(".wav") ? "audio/wav" :
                        ext.equals(".ogg") ? "audio/ogg" : "application/octet-stream";
                    org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
                    return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(resource);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping({"/audio/sentences/{sentenceId}"})
    public ResponseEntity<org.springframework.core.io.Resource> getSentenceAudio(@PathVariable Long sentenceId) {
        try {
            // DB에서 audioUrl을 가져옴
            Sentence sentence = sentenceRepository.findById(sentenceId).orElse(null);
            String audioUrl = (sentence != null) ? sentence.getAudioUrl() : null;
            if (audioUrl != null && !audioUrl.isEmpty()) {
                String filePath = System.getProperty("user.dir") + audioUrl;
                java.io.File audioFile = new java.io.File(filePath);
                if (audioFile.exists()) {
                    String contentType = null;
                    String lower = audioUrl.toLowerCase();
                    if (lower.endsWith(".mp3")) contentType = "audio/mpeg";
                    else if (lower.endsWith(".m4a")) contentType = "audio/mp4";
                    else if (lower.endsWith(".wav")) contentType = "audio/wav";
                    else if (lower.endsWith(".ogg")) contentType = "audio/ogg";
                    else contentType = "application/octet-stream";
                    org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(audioFile);
                    return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(resource);
                }
            }
            // 기존 방식: 확장자별로 직접 확인 (백워드 호환)
            String[] exts = {".mp3", ".m4a", ".wav", ".ogg"};
            for (String ext : exts) {
                String path = System.getProperty("user.dir") + "/audio/sentences/" + sentenceId + ext;
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    String contentType =
                        ext.equals(".mp3") ? "audio/mpeg" :
                        ext.equals(".m4a") ? "audio/mp4" :
                        ext.equals(".wav") ? "audio/wav" :
                        ext.equals(".ogg") ? "audio/ogg" : "application/octet-stream";
                    org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
                    return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(resource);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping({"/analytics/user-progress"})
    public ResponseEntity<List<Map<String, Object>>> getUserProgress() {
        List<Map<String, Object>> progress = this.adminService.getUserProgress();
        return ResponseEntity.ok(progress);
    }

    @GetMapping({"/analytics/word-stats"})
    public ResponseEntity<List<Map<String, Object>>> getWordStats() {
        List<Map<String, Object>> stats = this.adminService.getWordStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping({"/analytics/daily-stats"})
    public ResponseEntity<Map<String, Object>> getDailyStats(@RequestParam String date) {
        Map<String, Object> stats = this.adminService.getDailyStats(date);
        return ResponseEntity.ok(stats);
    }

    @GetMapping({"/coins/settings"})
    public ResponseEntity<LearningSettingsDto> getCoinSettings() {
        LearningSettingsDto settings = this.adminService.getLearningSettings();
        return ResponseEntity.ok(settings);
    }

    @PostMapping({"/coins/settings"})
    public ResponseEntity<LearningSettingsDto> updateCoinSettings(@RequestBody LearningSettingsDto settingsDto) {
        LearningSettingsDto updatedSettings = this.adminService.updateLearningSettings(settingsDto);
        return ResponseEntity.ok(updatedSettings);
    }

    // === 기존 오디오 파일명 정규화 엔드포인트 추가 ===
    @PostMapping("/normalize-audio-filenames")
    public ResponseEntity<Map<String, Object>> normalizeAudioFileNames() {
        try {
            adminService.normalizeExistingAudioFileNames();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "오디오 파일명 정규화가 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("오디오 파일명 정규화 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "오디오 파일명 정규화 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 단일 업로드
    @PostMapping("/audio/upload")
    public ResponseEntity<?> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type // "words" or "sentences"
    ) {
        try {
            String s3Key = s3Service.uploadFile(file, type);
            return ResponseEntity.ok().body("{\"s3Key\": \"" + s3Key + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // (옵션) 여러 파일 bulk 업로드
    @PostMapping("/audio/bulk-upload")
    public ResponseEntity<?> bulkUploadAudio(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("type") String type
    ) {
        try {
            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                String url = s3Service.uploadFile(file, type);
                uploadedUrls.add(url);
            }
            return ResponseEntity.ok(Map.of("message", "파일들이 성공적으로 업로드되었습니다.", "urls", uploadedUrls));
        } catch (Exception e) {
            log.error("Bulk audio upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/templates/sentence-upload")
    public ResponseEntity<org.springframework.core.io.Resource> downloadSentenceTemplate() {
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource("templates/sentence_upload_template.csv");
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"sentence_upload_template.csv\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Template download failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/templates/audio-filenames")
    public ResponseEntity<org.springframework.core.io.Resource> downloadAudioFilenamesTemplate() {
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource("templates/audio_filenames_for_copy.txt");
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"audio_filenames_for_copy.txt\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Audio filenames template download failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName(); // 이메일 또는 username
        }
        return "anonymous"; // 기본값
    }

    @Generated
    public AdminController(final AdminService adminService) {
        this.adminService = adminService;
    }
}
