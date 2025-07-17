package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Builder.Default
    private Boolean termsRequired = true; // 이용약관 필수 여부
    
    @Column(columnDefinition = "TEXT")
    private String termsContent; // 이용약관 내용
    
    @Builder.Default
    private Boolean privacyRequired = true; // 개인정보 수집 필수 여부
    
    @Column(columnDefinition = "TEXT")
    private String privacyContent; // 개인정보 수집 내용
    
    @Builder.Default
    private Boolean marketingRequired = false; // 마케팅 정보 수신 필수 여부
    
    @Column(columnDefinition = "TEXT")
    private String marketingContent; // 마케팅 정보 수신 내용
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 