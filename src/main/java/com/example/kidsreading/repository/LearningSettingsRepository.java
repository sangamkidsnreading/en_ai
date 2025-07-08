package com.example.kidsreading.repository;


import com.example.kidsreading.entity.LearningSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningSettingsRepository extends JpaRepository<LearningSettings, Long> {
    Optional<LearningSettings> findFirstByOrderByIdDesc();
    Optional<LearningSettings> findTopByOrderByCreatedAtDesc(); // ScopedValue를 Optional로 변경
}