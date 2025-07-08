package com.example.kidsreading.repository;

import com.example.kidsreading.entity.SentenceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceCategoryRepository extends JpaRepository<SentenceCategory, Long> {
    
    List<SentenceCategory> findByIsActiveTrue();
    
    List<SentenceCategory> findByCategoryNameContainingIgnoreCase(String categoryName);
    
    boolean existsByCategoryNameAndIsActiveTrue(String categoryName);
} 