// ========== AuthController.java (업데이트) ==========
package com.example.kidsreading.controller;

import com.example.kidsreading.dto.RegisterRequest;
import com.example.kidsreading.service.UserService;
import com.example.kidsreading.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final RegistrationService registrationService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model, HttpServletRequest request) {
        if (error != null) {
            String errorMsg = (String) request.getSession().getAttribute("loginError");
            if (errorMsg == null || errorMsg.isBlank()) {
                errorMsg = "이메일 또는 비밀번호가 올바르지 않습니다.";
            }
            model.addAttribute("error", errorMsg);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        // 회원가입 성공 메시지 표시
        String signupSuccess = (String) request.getSession().getAttribute("signupSuccess");
        if (signupSuccess != null) {
            model.addAttribute("signupSuccess", signupSuccess);
            request.getSession().removeAttribute("signupSuccess");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        // 분원 목록과 동의 설정 추가
        model.addAttribute("groupList", registrationService.getActiveGroups());
        model.addAttribute("registrationSettings", registrationService.getRegistrationSettings());
        log.info("registrationSettings: {}", registrationService.getRegistrationSettings());
        return "auth/register";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/student/kiriboca/index";
        }
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "student/kiriboca/index";
    }

    @GetMapping("/student/kiriboca/index")
    public String kiribocaIndex(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        // principal에서 email 꺼내기
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof com.example.kidsreading.service.CustomUserDetailsService.CustomUserPrincipal customUserPrincipal) {
            email = customUserPrincipal.getUser().getEmail();
        } else {
            email = authentication.getName();
        }
        com.example.kidsreading.entity.User user = userService.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
        model.addAttribute("role", user.getRole().name());
        model.addAttribute("currentLevel", 1);
        model.addAttribute("currentDay", 1);
        model.addAttribute("name", user.getName());
        return "student/kiriboca/index";
    }

    /**
     * 이메일 인증번호 전송 API
     */
    @PostMapping("/api/auth/send-verification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.sendVerificationCode(email);
            response.put("success", true);
            response.put("message", "인증번호가 전송되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 인증번호 확인 API
     */
    @PostMapping("/api/auth/verify-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestParam String email,
                                                          @RequestParam String code) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isValid = userService.verifyCode(email, code);
            response.put("success", isValid);
            response.put("message", isValid ? "인증이 완료되었습니다." : "인증번호가 올바르지 않습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/register")
    public String registerProcess(@Valid @ModelAttribute RegisterRequest registerRequest,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.error("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
            StringBuilder errorMsg = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> errorMsg.append(e.getDefaultMessage()).append("\n"));
            model.addAttribute("error", errorMsg.toString().trim());
            model.addAttribute("registerRequest", registerRequest);
            // 분원 목록과 동의 설정 추가
            model.addAttribute("groupList", registrationService.getActiveGroups());
            model.addAttribute("registrationSettings", registrationService.getRegistrationSettings());
            return "auth/register";
        }

        try {
            userService.registerUser(registerRequest);
            // 회원가입 성공 메시지 추가
            redirectAttributes.addFlashAttribute("signupSuccess", "관리자 승인 후 이용 가능합니다. 감사합니다.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            // 분원 목록과 동의 설정 추가
            model.addAttribute("groupList", registrationService.getActiveGroups());
            model.addAttribute("registrationSettings", registrationService.getRegistrationSettings());
            return "auth/register";
        }
    }
}