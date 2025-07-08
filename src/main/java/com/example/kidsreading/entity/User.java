// ========== User.java (확장된 엔티티) ==========
package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    // 회원가입 폼에서 추가된 필드들
    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "group_name")
    private String groupName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default  // Add this annotation
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default  // Add this annotation
    private Boolean emailVerified = false;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserWordProgress> wordProgresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserSentenceProgress> sentenceProgresses;

    public enum Role {
        ADMIN, STUDENT, PARENT
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}