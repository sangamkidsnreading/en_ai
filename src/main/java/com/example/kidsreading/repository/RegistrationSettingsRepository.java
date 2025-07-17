package com.example.kidsreading.repository;

import com.example.kidsreading.entity.RegistrationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationSettingsRepository extends JpaRepository<RegistrationSettings, Long> {
    
    // 기본 설정은 하나만 존재
    RegistrationSettings findFirstByOrderByIdAsc();
} 