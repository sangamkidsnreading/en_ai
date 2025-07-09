
package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private String meaning;

    private String pronunciation;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer day;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 호환성을 위한 별칭 메서드들
    public String getEnglish() {
        return this.text;
    }

    public void setEnglish(String english) {
        this.text = english;
    }

    public String getKorean() {
        return this.meaning;
    }

    public void setKorean(String korean) {
        this.meaning = korean;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
