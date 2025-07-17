//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.kidsreading.repository;

import com.example.kidsreading.entity.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    
    List<Sentence> findByIsActiveTrue();

    List<Sentence> findByDifficultyLevelAndIsActiveTrue(Integer difficultyLevel);

    @Query("SELECT s FROM Sentence s WHERE s.isActive = true AND (LOWER(s.englishText) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.koreanTranslation) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Sentence> searchByQuery(@Param("query") String query);

    long countByDifficultyLevelAndIsActiveTrue(Integer difficultyLevel);

    long countByIsActiveTrue();

    boolean existsByEnglishTextAndIsActiveTrue(String englishText);

    List<Sentence> findByDifficultyLevelAndDayNumberAndIsActiveOrderById(Integer difficultyLevel, Integer dayNumber, Boolean isActive);

    int countByDifficultyLevelAndDayNumberAndIsActive(Integer difficultyLevel, Integer dayNumber, Boolean isActive);

    List<Sentence> findByDifficultyLevelAndIsActiveOrderByDayNumberAscIdAsc(Integer difficultyLevel, Boolean isActive);

    @Query("SELECT s FROM Sentence s WHERE s.isActive = true AND (LOWER(s.englishText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.koreanTranslation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.phonetic) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Sentence> searchSentences(@Param("keyword") String keyword);

    List<Sentence> findByIdInAndIsActive(List<Long> ids, Boolean isActive);

    List<Sentence> findByDifficultyLevelAndDayNumberAndIsActiveTrue(Integer difficultyLevel, Integer dayNumber);

    List<Sentence> findByIsActiveTrueOrderByDifficultyLevelAscDayNumberAsc();

    List<Sentence> findByDayNumberAndIsActiveTrueOrderByDifficultyLevelAsc(Integer dayNumber);

    List<Sentence> findByDifficultyLevelAndIsActiveTrueOrderByDayNumberAsc(Integer difficultyLevel);
    
    List<Sentence> findByCategoryIdAndIsActiveTrue(Long categoryId);
    
    List<Sentence> findByCategoryIdAndDifficultyLevelAndIsActiveTrue(Long categoryId, Integer difficultyLevel);

    @Query("SELECT COUNT(sp) FROM UserSentenceProgress sp WHERE sp.userId = :userId AND sp.isCompleted = true AND FUNCTION('date', sp.updatedAt) = CURRENT_DATE")
    int getTodayCompletedSentencesCount(@Param("userId") Long userId);

    List<Sentence> findByDifficultyLevelAndDayNumberAndIsActiveTrueOrderByDisplayOrderAscIdAsc(Integer difficultyLevel, Integer dayNumber);

    // S3 key(확장자 없는 audioUrl)로 Sentence 단건 조회
    Sentence findByAudioUrl(String audioUrl);

    // 폴더 기준으로 audioUrl prefix로 문장 리스트 조회
    java.util.List<Sentence> findByAudioUrlStartingWith(String prefix);

    // audioUrl이 특정 prefix로 시작하는 문장들 조회
    List<Sentence> findByAudioUrlStartingWithOrderByIdAsc(String prefix);
}
