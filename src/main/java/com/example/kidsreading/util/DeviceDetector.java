package com.example.kidsreading.util;

import org.springframework.stereotype.Component;

@Component
public class DeviceDetector {
    
    /**
     * User-Agent 문자열을 분석하여 디바이스 타입을 반환
     * @param userAgent User-Agent 문자열
     * @return 디바이스 타입 (PC, Tablet, Mobile)
     */
    public String detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        String ua = userAgent.toLowerCase();
        
        // 태블릿 감지
        if (isTablet(ua)) {
            return "Tablet";
        }
        
        // 모바일 감지
        if (isMobile(ua)) {
            return "Mobile";
        }
        
        // PC (데스크톱/노트북)
        return "PC";
    }
    
    /**
     * 태블릿 디바이스인지 확인
     */
    private boolean isTablet(String userAgent) {
        return userAgent.contains("ipad") ||
               userAgent.contains("tablet") ||
               userAgent.contains("playbook") ||
               userAgent.contains("silk") ||
               (userAgent.contains("android") && !userAgent.contains("mobile")) ||
               userAgent.contains("kindle") ||
               userAgent.contains("tablet pc");
    }
    
    /**
     * 모바일 디바이스인지 확인
     */
    private boolean isMobile(String userAgent) {
        return userAgent.contains("mobile") ||
               userAgent.contains("android") ||
               userAgent.contains("iphone") ||
               userAgent.contains("ipod") ||
               userAgent.contains("blackberry") ||
               userAgent.contains("windows phone") ||
               userAgent.contains("opera mini") ||
               userAgent.contains("opera mobi") ||
               userAgent.contains("iemobile") ||
               userAgent.contains("webos") ||
               userAgent.contains("palm") ||
               userAgent.contains("symbian") ||
               userAgent.contains("nokia");
    }
} 