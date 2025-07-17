package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

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
    @Builder.Default
    private Boolean isActive = false;

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

    @Column(name = "level")
    private Integer level;

    @Column(name = "level_progress")
    private Integer levelProgress;

    @Column(name = "words_to_next_level")
    private Integer wordsToNextLevel;

    // 회원가입 동의 필드들
    @Column(name = "agree_terms")
    @Builder.Default
    private Boolean agreeTerms = false;

    @Column(name = "agree_privacy")
    @Builder.Default
    private Boolean agreePrivacy = false;

    @Column(name = "agree_marketing")
    @Builder.Default
    private Boolean agreeMarketing = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserWordProgress> wordProgresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserSentenceProgress> sentenceProgresses;

    public enum Role {
        ADMIN,
        USER,
        STUDENT,
        PARENT,
        TEACHER,
        SANGAM,
        YONGIN
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

    // ================= UserDetails 구현 =================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}