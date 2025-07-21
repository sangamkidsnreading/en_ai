package com.example.kidsreading.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IpAddressUtil {
    
    /**
     * HTTP 요청에서 클라이언트의 실제 IP 주소를 추출
     * @param request HTTP 요청 객체
     * @return 클라이언트 IP 주소
     */
    public String getClientIpAddress(HttpServletRequest request) {
        // X-Forwarded-For 헤더 확인 (프록시/로드밸런서 환경)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For는 쉼표로 구분된 IP 목록이므로 첫 번째 IP 사용
            return xForwardedFor.split(",")[0].trim();
        }
        
        // X-Real-IP 헤더 확인
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        // X-Forwarded 헤더 확인
        String xForwarded = request.getHeader("X-Forwarded");
        if (StringUtils.hasText(xForwarded) && !"unknown".equalsIgnoreCase(xForwarded)) {
            return xForwarded;
        }
        
        // Proxy-Client-IP 헤더 확인
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (StringUtils.hasText(proxyClientIp) && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        // WL-Proxy-Client-IP 헤더 확인 (WebLogic)
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.hasText(wlProxyClientIp) && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        // HTTP_CLIENT_IP 헤더 확인
        String httpClientIp = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtils.hasText(httpClientIp) && !"unknown".equalsIgnoreCase(httpClientIp)) {
            return httpClientIp;
        }
        
        // HTTP_X_FORWARDED_FOR 헤더 확인
        String httpXForwardedFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.hasText(httpXForwardedFor) && !"unknown".equalsIgnoreCase(httpXForwardedFor)) {
            return httpXForwardedFor.split(",")[0].trim();
        }
        
        // 직접 연결된 클라이언트의 IP 주소
        String remoteAddr = request.getRemoteAddr();
        if (StringUtils.hasText(remoteAddr) && !"unknown".equalsIgnoreCase(remoteAddr)) {
            return remoteAddr;
        }
        
        // 기본값
        return "0.0.0.0";
    }
    
    /**
     * IP 주소가 유효한지 확인
     * @param ipAddress IP 주소 문자열
     * @return 유효한 IP 주소인지 여부
     */
    public boolean isValidIpAddress(String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return false;
        }
        
        // IPv4 패턴 확인
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (ipAddress.matches(ipv4Pattern)) {
            return true;
        }
        
        // IPv6 패턴 확인 (간단한 검증)
        if (ipAddress.contains(":")) {
            return true;
        }
        
        return false;
    }
} 