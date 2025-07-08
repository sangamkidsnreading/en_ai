package com.example.kidsreading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // JavaScript 파일들을 위한 핸들러
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true);

        // 키리보카 JavaScript 파일들을 위한 핸들러
        registry.addResourceHandler("/student/kiriboca/js/**")
                .addResourceLocations("classpath:/static/student/kiriboca/js/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true);

        // CSS 파일들을 위한 핸들러
        registry.addResourceHandler("/student/kiriboca/css/**")
                .addResourceLocations("classpath:/static/student/kiriboca/css/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true);

        // 일반 CSS 파일들
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true);

        // 이미지 파일들
        registry.addResourceHandler("/img/**", "/images/**")
                .addResourceLocations("classpath:/static/img/", "classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .resourceChain(true);

        // 오디오 파일들
        registry.addResourceHandler("/audio/**")
                .addResourceLocations("classpath:/static/audio/")
                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .resourceChain(true);

        // 업로드된 파일들
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/")
                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .resourceChain(true);

        // 기타 정적 리소스
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true);

        // Webjars 지원 (Bootstrap, jQuery 등)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .resourceChain(true);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 모든 도메인 허용 (개발 환경용)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1시간
    }
}