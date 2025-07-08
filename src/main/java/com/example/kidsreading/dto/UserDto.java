package com.example.kidsreading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String password; // 생성/수정 시에만 사용
    private String role;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}