package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "words")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "meaning")
    private String meaning;

    @Column(name = "pronunciation")
    private String pronunciation;

    @Column(name = "phonetic")
    private String phonetic;

    @Column(name = "level")
    private Integer level;

    @Column(name = "day")
    private Integer day;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "day_number")
    private Integer dayNumber;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "audio_file")
    private String audioFile;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "category")
    private String category;

    @Column(name = "part_of_speech")
    private String partOfSpeech;

    @Column(name = "example_sentence")
    private String exampleSentence;

    @Column(name = "example_translation")
    private String exampleTranslation;

    @Column(name = "synonyms")
    private String synonyms;

    @Column(name = "antonyms")
    private String antonyms;

    @Column(name = "frequency_rank")
    private Integer frequencyRank;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserWordProgress> userProgresses = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getAudioUrl() {
        return audioUrl != null ? audioUrl : audioFile;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAudioFile() {
        return audioFile != null ? audioFile : audioUrl;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }
}