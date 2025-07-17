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

    List<Word> findByLevelAndDayAndIsActiveTrueOrderByDisplayOrderAscIdAsc(Integer level, Integer day);

    int countByLevelAndDayAndIsActiveTrue(Integer level, Integer day);

    /**
     * 활성화된 단어들의 고유 레벨 목록 조회
     */
    @Query("SELECT DISTINCT w.level FROM Word w WHERE w.isActive = true ORDER BY w.level")
    List<Integer> findDistinctLevelsByIsActiveTrueOrderByLevel();

    /**
     * 특정 레벨의 활성화된 단어들의 고유 날짜 목록 조회
     */
    @Query("SELECT DISTINCT w.day FROM Word w WHERE w.level = :level AND w.isActive = true ORDER BY w.day")
    List<Integer> findDistinctDaysByLevelAndIsActiveTrueOrderByDay(@Param("level") Integer level);

    @Query("SELECT COUNT(wp) FROM UserWordProgress wp WHERE wp.userId = :userId AND wp.isLearned = true AND FUNCTION('date', wp.updatedAt) = CURRENT_DATE")
    int getTodayCompletedWordsCount(@Param("userId") Long userId);

    // audioUrl이 특정 prefix로 시작하는 단어 리스트 조회
    List<Word> findByAudioUrlStartingWith(String prefix);
}
