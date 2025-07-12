// ========== AuthController.java (업데이트) ==========
package com.example.kidsreading.controller;

import com.example.kidsreading.dto.RegisterRequest;
import com.example.kidsreading.service.UserService;
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

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
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
        String email = authentication.getName();
        com.example.kidsreading.entity.User user = userService.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
        model.addAttribute("role", user.getRole().name());
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
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.error("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
            return "redirect:/register";
        }

        try {
            userService.registerUser(registerRequest);
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}