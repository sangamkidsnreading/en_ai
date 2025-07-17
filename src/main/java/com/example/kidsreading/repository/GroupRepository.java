package com.example.kidsreading.repository;

import com.example.kidsreading.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    Optional<Group> findByCode(String code);
    
    List<Group> findByIsActiveTrue();
    
    boolean existsByCode(String code);
} 