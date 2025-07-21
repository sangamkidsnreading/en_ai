package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    // 사용자의 모든 뱃지 조회
    List<UserBadge> findByUserIdOrderByEarnedAtDesc(Long userId);
    
    // 사용자의 표시 가능한 뱃지 조회
    List<UserBadge> findByUserIdAndIsDisplayedTrueOrderByEarnedAtDesc(Long userId);
    
    // 특정 뱃지를 이미 획득했는지 확인
    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, Long badgeId);
    
    // 사용자가 획득한 뱃지 개수
    long countByUserId(Long userId);
    
    // 사용자가 표시 가능한 뱃지 개수
    long countByUserIdAndIsDisplayedTrue(Long userId);
    
    // 특정 타입의 뱃지를 획득한 사용자 수
    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.badge.id = :badgeId")
    long countByBadgeId(@Param("badgeId") Long badgeId);
    
    // 사용자의 최근 획득 뱃지 (최대 5개)
    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId ORDER BY ub.earnedAt DESC")
    List<UserBadge> findRecentBadgesByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
} 