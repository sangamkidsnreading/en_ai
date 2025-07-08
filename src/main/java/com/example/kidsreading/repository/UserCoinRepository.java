package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    Optional<UserCoin> findByUserIdAndDate(String userId, LocalDate date);

    List<UserCoin> findByUserIdOrderByDateDesc(String userId);

    @Query("SELECT SUM(uc.totalCoins) FROM UserCoin uc WHERE uc.userId = :userId")
    Integer getTotalCoinsByUserId(@Param("userId") String userId);

    @Query("SELECT uc FROM UserCoin uc WHERE uc.userId = :userId AND uc.date BETWEEN :startDate AND :endDate ORDER BY uc.date DESC")
    List<UserCoin> findByUserIdAndDateBetween(@Param("userId") String userId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(uc) FROM UserCoin uc WHERE uc.userId = :userId AND uc.date >= :date")
    Long countConsecutiveDays(@Param("userId") String userId, @Param("date") LocalDate date);

    @Query("SELECT uc FROM UserCoin uc WHERE uc.date = :date")
    List<UserCoin> findByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(uc.coinsEarned) FROM UserCoin uc WHERE uc.date = :date")
    Integer getTotalCoinsEarnedByDate(@Param("date") LocalDate date);

    void deleteByUserIdAndDate(String userId, LocalDate date);
}