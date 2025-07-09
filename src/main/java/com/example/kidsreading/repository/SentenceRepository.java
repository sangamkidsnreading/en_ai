package com.example.kidsreading.repository;

import com.example.kidsreading.entity.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceRepository extends JpaRepository<Sentence, Long> {

    List<Sentence> findByDifficultyLevelAndDayNumberAndIsActiveTrue(Integer level, Integer day);

    List<Sentence> findByDifficultyLevelAndIsActiveTrue(Integer level);

    List<Sentence> findByIsActiveTrue();

    long countByDifficultyLevelAndDayNumberAndIsActiveTrue(Integer level, Integer day);

    @Query("SELECT s FROM Sentence s WHERE s.difficultyLevel = :level AND s.dayNumber = :day AND s.isActive = true")
    List<Sentence> findActiveSentencesByLevelAndDay(@Param("level") Integer level, @Param("day") Integer day);

    long countByDifficultyLevelAndIsActiveTrue(Integer difficultyLevel);

    boolean existsByEnglishTextAndIsActiveTrue(String englishText);

    List<Sentence> findByDifficultyLevelAndDayNumberAndIsActiveOrderById(Integer difficultyLevel, Integer dayNumber, Boolean isActive);

    int countByDifficultyLevelAndDayNumberAndIsActive(Integer difficultyLevel, Integer dayNumber, Boolean isActive);

    List<Sentence> findByDifficultyLevelAndIsActiveOrderByDayNumberAscIdAsc(Integer difficultyLevel, Boolean isActive);

    @Query("SELECT s FROM Sentence s WHERE s.isActive = true AND (LOWER(s.englishText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.koreanTranslation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.phonetic) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Sentence> searchSentences(@Param("keyword") String keyword);

    List<Sentence> findByIdInAndIsActive(List<Long> ids, Boolean isActive);

    List<Sentence> findByIsActiveTrueOrderByDifficultyLevelAscDayNumberAsc();

    List<Sentence> findByDayNumberAndIsActiveTrueOrderByDifficultyLevelAsc(Integer dayNumber);

    List<Sentence> findByDifficultyLevelAndIsActiveTrueOrderByDayNumberAsc(Integer difficultyLevel);

    List<Sentence> findByCategoryIdAndIsActiveTrue(Long categoryId);

    List<Sentence> findByCategoryIdAndDifficultyLevelAndIsActiveTrue(Long categoryId, Integer difficultyLevel);

    long countByIsActiveTrue();
}