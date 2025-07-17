package com.example.kidsreading.repository;

import com.example.kidsreading.entity.LevelSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LevelSettingsRepository extends JpaRepository<LevelSettings, Long> {
    Optional<LevelSettings> findByLevel(int level);
    List<LevelSettings> findByLevelLessThan(int level);
}
