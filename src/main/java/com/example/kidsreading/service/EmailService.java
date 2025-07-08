// ========== EmailService.java ==========
package com.example.kidsreading.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    /**
     * 인증번호 이메일 전송 (Mock 구현)
     */
    public void sendVerificationCode(String email, String verificationCode) {
        // 실제 구현에서는 메일 서비스 (예: SendGrid, AWS SES 등) 사용
        log.info("===== 이메일 인증번호 전송 =====");
        log.info("수신자: {}", email);
        log.info("인증번호: {}", verificationCode);
        log.info("==============================");

        // 개발 환경에서는 콘솔에 출력
        System.out.println("📧 이메일 인증번호: " + verificationCode);
    }
}