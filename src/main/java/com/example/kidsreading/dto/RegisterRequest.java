// ========== RegisterRequest.java (DTO) ==========
package com.example.kidsreading.dto;

import com.example.kidsreading.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "비밀번호는 영문, 숫자, 특수문자(@$!%*?&)를 포함하여 8-16자리여야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 10, message = "이름은 2-10자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "보호자 이름은 필수입니다.")
    @Size(min = 2, max = 10, message = "보호자 이름은 2-10자 사이여야 합니다.")
    private String parentName;

    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    @NotBlank(message = "연락처는 필수입니다.")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNumber;

    @NotBlank(message = "그룹 선택은 필수입니다.")
    private String groupName;

    private Boolean agreeTerms;
    private Boolean agreePrivacy;
    private Boolean agreeMarketing;

    // 기본값들 (숨겨진 필드로 전송됨)
    @Builder.Default
    private User.Role role = User.Role.STUDENT;

    @Builder.Default
    private Boolean isActive = false;

    @Builder.Default
    private Boolean emailVerified = false;

    // 사용자명은 이메일과 동일하게 설정
    public String getUsername() {
        return this.email;
    }

    public Boolean getAgreeTerms() {
        return agreeTerms;
    }
    public void setAgreeTerms(Boolean agreeTerms) {
        this.agreeTerms = agreeTerms;
    }

    public Boolean getAgreePrivacy() {
        return agreePrivacy;
    }
    public void setAgreePrivacy(Boolean agreePrivacy) {
        this.agreePrivacy = agreePrivacy;
    }

    public Boolean getAgreeMarketing() {
        return agreeMarketing;
    }
    public void setAgreeMarketing(Boolean agreeMarketing) {
        this.agreeMarketing = agreeMarketing;
    }
}