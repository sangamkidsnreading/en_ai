package com.example.kidsreading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationSettingsDto {
    
    private Long id;
    private Boolean termsRequired;
    private String termsContent;
    private Boolean privacyRequired;
    private String privacyContent;
    private Boolean marketingRequired;
    private String marketingContent;
} 