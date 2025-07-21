package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {
    
    // 사용자 ID로 연속 학습 정보 조회
    Optional<UserStreak> findByUserId(Long userId);
    
    // 사용자 ID로 연속 학습 정보 존재 여부 확인
    boolean existsByUserId(Long userId);
    
    // 이메일로 연속 학습 정보 조회
    Optional<UserStreak> findByEmail(String email);
} 