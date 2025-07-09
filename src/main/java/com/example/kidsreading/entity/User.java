package com.example.kidsreading.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String parentName;
    private String phoneNumber;
    private String groupName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;

    private Boolean isActive = true;
    private Boolean emailVerified = false;

    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "birth_date")
    private LocalDate birthDate;
    private LocalDateTime lastLogin;

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

    // Getter methods
    public Boolean getIsActive() {
        return isActive;
    }
}