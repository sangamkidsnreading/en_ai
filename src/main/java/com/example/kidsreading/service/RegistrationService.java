package com.example.kidsreading.service;

import com.example.kidsreading.dto.GroupDto;
import com.example.kidsreading.dto.RegistrationSettingsDto;
import com.example.kidsreading.entity.Group;
import com.example.kidsreading.entity.RegistrationSettings;
import com.example.kidsreading.repository.GroupRepository;
import com.example.kidsreading.repository.RegistrationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {
    
    private final GroupRepository groupRepository;
    private final RegistrationSettingsRepository registrationSettingsRepository;
    
    // === 분원 관리 ===
    
    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::convertToGroupDto)
                .collect(Collectors.toList());
    }
    
    public List<GroupDto> getActiveGroups() {
        return groupRepository.findByIsActiveTrue().stream()
                .map(this::convertToGroupDto)
                .collect(Collectors.toList());
    }
    
    public GroupDto getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("분원을 찾을 수 없습니다."));
        return convertToGroupDto(group);
    }
    
    public GroupDto createGroup(GroupDto groupDto) {
        if (groupRepository.existsByCode(groupDto.getCode())) {
            throw new RuntimeException("이미 존재하는 분원 코드입니다.");
        }
        
        Group group = Group.builder()
                .code(groupDto.getCode())
                .name(groupDto.getName())
                .address(groupDto.getAddress())
                .phone(groupDto.getPhone())
                .isActive(groupDto.getIsActive() != null ? groupDto.getIsActive() : true)
                .build();
        
        Group savedGroup = groupRepository.save(group);
        return convertToGroupDto(savedGroup);
    }
    
    public GroupDto updateGroup(Long id, GroupDto groupDto) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("분원을 찾을 수 없습니다."));
        
        // 코드가 변경된 경우 중복 체크
        if (!group.getCode().equals(groupDto.getCode()) && 
            groupRepository.existsByCode(groupDto.getCode())) {
            throw new RuntimeException("이미 존재하는 분원 코드입니다.");
        }
        
        group.setCode(groupDto.getCode());
        group.setName(groupDto.getName());
        group.setAddress(groupDto.getAddress());
        group.setPhone(groupDto.getPhone());
        group.setIsActive(groupDto.getIsActive());
        
        Group savedGroup = groupRepository.save(group);
        return convertToGroupDto(savedGroup);
    }
    
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new RuntimeException("분원을 찾을 수 없습니다.");
        }
        groupRepository.deleteById(id);
    }
    
    // === 회원가입 설정 관리 ===
    
    public RegistrationSettingsDto getRegistrationSettings() {
        RegistrationSettings settings = registrationSettingsRepository.findFirstByOrderByIdAsc();
        if (settings == null) {
            // 기본 설정 생성
            settings = RegistrationSettings.builder()
                    .termsRequired(true)
                    .privacyRequired(true)
                    .marketingRequired(false)
                    .termsContent("이용약관 내용을 입력하세요.")
                    .privacyContent("개인정보 수집 및 이용 동의 내용을 입력하세요.")
                    .marketingContent("마케팅 정보 수신 동의 내용을 입력하세요.")
                    .build();
            settings = registrationSettingsRepository.save(settings);
        }
        return convertToRegistrationSettingsDto(settings);
    }
    
    public RegistrationSettingsDto updateRegistrationSettings(RegistrationSettingsDto settingsDto) {
        RegistrationSettings settings = registrationSettingsRepository.findFirstByOrderByIdAsc();
        if (settings == null) {
            settings = new RegistrationSettings();
        }
        
        settings.setTermsRequired(settingsDto.getTermsRequired());
        settings.setTermsContent(settingsDto.getTermsContent());
        settings.setPrivacyRequired(settingsDto.getPrivacyRequired());
        settings.setPrivacyContent(settingsDto.getPrivacyContent());
        settings.setMarketingRequired(settingsDto.getMarketingRequired());
        settings.setMarketingContent(settingsDto.getMarketingContent());
        
        RegistrationSettings savedSettings = registrationSettingsRepository.save(settings);
        return convertToRegistrationSettingsDto(savedSettings);
    }
    
    // === 변환 메서드 ===
    
    private GroupDto convertToGroupDto(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .code(group.getCode())
                .name(group.getName())
                .address(group.getAddress())
                .phone(group.getPhone())
                .isActive(group.getIsActive())
                .build();
    }
    
    private RegistrationSettingsDto convertToRegistrationSettingsDto(RegistrationSettings settings) {
        return RegistrationSettingsDto.builder()
                .id(settings.getId())
                .termsRequired(settings.getTermsRequired())
                .termsContent(settings.getTermsContent())
                .privacyRequired(settings.getPrivacyRequired())
                .privacyContent(settings.getPrivacyContent())
                .marketingRequired(settings.getMarketingRequired())
                .marketingContent(settings.getMarketingContent())
                .build();
    }
} 