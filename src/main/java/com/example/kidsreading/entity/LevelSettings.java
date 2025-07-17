package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "level_settings")
public class LevelSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int level;

    @Column(nullable = false)
    private int wordsToNextLevel;

    @Column(nullable = false)
    private int sentencesToNextLevel;

    @Column(length = 100)
    private String updatedBy;

    private LocalDateTime updatedAt;

    // getters/setters

    public void setWordsToNextLevel(int wordsToNextLevel) {
        this.wordsToNextLevel = wordsToNextLevel;
    }
}
