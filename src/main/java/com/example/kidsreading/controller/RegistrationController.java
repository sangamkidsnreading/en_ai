package com.example.kidsreading.controller;

import com.example.kidsreading.dto.GroupDto;
import com.example.kidsreading.dto.RegistrationSettingsDto;
import com.example.kidsreading.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/registration")
@RequiredArgsConstructor
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    // === 분원 관리 API ===
    
    @GetMapping("/groups")
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        List<GroupDto> groups = registrationService.getAllGroups();
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/groups/active")
    public ResponseEntity<List<GroupDto>> getActiveGroups() {
        List<GroupDto> groups = registrationService.getActiveGroups();
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/groups/{id}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long id) {
        try {
            GroupDto group = registrationService.getGroupById(id);
            return ResponseEntity.ok(group);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody GroupDto groupDto) {
        try {
            GroupDto createdGroup = registrationService.createGroup(groupDto);
            return ResponseEntity.ok(createdGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/groups/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Long id, @RequestBody GroupDto groupDto) {
        try {
            GroupDto updatedGroup = registrationService.updateGroup(id, groupDto);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/groups/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        try {
            registrationService.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // === 회원가입 설정 API ===
    
    @GetMapping("/settings")
    public ResponseEntity<RegistrationSettingsDto> getRegistrationSettings() {
        RegistrationSettingsDto settings = registrationService.getRegistrationSettings();
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping("/settings")
    public ResponseEntity<RegistrationSettingsDto> updateRegistrationSettings(@RequestBody RegistrationSettingsDto settingsDto) {
        RegistrationSettingsDto updatedSettings = registrationService.updateRegistrationSettings(settingsDto);
        return ResponseEntity.ok(updatedSettings);
    }
} 