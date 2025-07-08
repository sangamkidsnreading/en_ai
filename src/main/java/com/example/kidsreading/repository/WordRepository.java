//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.kidsreading.repository;

import com.example.kidsreading.entity.Word;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByIsActiveTrue();

    List<Word> findByLevelAndIsActiveTrue(Integer level);

    @Query("SELECT w FROM Word w WHERE w.isActive = true AND (LOWER(w.text) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(w.meaning) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Word> searchByQuery(@Param("query") String query);

    long countByLevelAndIsActiveTrue(Integer level);

    long countByIsActiveTrue();

    boolean existsByTextAndIsActiveTrue(String text);

    List<Word> findByLevelAndDayAndIsActiveOrderById(Integer level, Integer day, Boolean isActive);

    int countByLevelAndDayAndIsActive(Integer level, Integer day, Boolean isActive);

    List<Word> findByLevelAndIsActiveOrderByDayAscIdAsc(Integer level, Boolean isActive);

    @Query("SELECT w FROM Word w WHERE w.isActive = true AND (LOWER(w.text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(w.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Word> searchWords(@Param("keyword") String keyword);

    List<Word> findByIdInAndIsActive(List<Long> ids, Boolean isActive);

    List<Word> findByLevelAndDayAndIsActiveTrue(Integer level, Integer day);

    List<Word> findByIsActiveTrueOrderByLevelAscDayAsc();

    List<Word> findByDayAndIsActiveTrueOrderByLevelAsc(Integer day);

    List<Word> findByLevelAndIsActiveTrueOrderByDayAsc(Integer level);

    @Query("SELECT DISTINCT w.level FROM Word w WHERE w.isActive = true ORDER BY w.level")
    List<Integer> findDistinctLevelsByIsActiveTrueOrderByLevel();

    @Query("SELECT DISTINCT w.day FROM Word w WHERE w.level = :level AND w.isActive = true ORDER BY w.day")
    List<Integer> findDistinctDaysByLevelAndIsActiveTrueOrderByDay(@Param("level") Integer level);

    int countByLevelAndDayAndIsActiveTrue(Integer level, Integer day);

    // 통계용 쿼리들
    @Query("SELECT COUNT(w) FROM Word w WHERE w.level = :level AND w.day = :day AND w.isActive = true")
    int countByLevelAndDayAndIsActiveTrue(@Param("level") Integer level, @Param("day") Integer day);

    // 사용 가능한 레벨/Day 조회
    @Query("SELECT DISTINCT w.level FROM Word w WHERE w.isActive = true ORDER BY w.level")
    List<Integer> findDistinctLevels();

    @Query("SELECT DISTINCT w.day FROM Word w WHERE w.level = :level AND w.isActive = true ORDER BY w.day")
    List<Integer> findDistinctDaysByLevel(@Param("level") Integer level);
}