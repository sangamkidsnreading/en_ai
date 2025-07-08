package com.example.kidsreading.config;

import com.example.kidsreading.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/student/kiriboca/index");
        handler.setAlwaysUseDefaultTargetUrl(true); // 무조건 해당 URL로 이동
        return handler;n handler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // 개발 환경에서는 완전 비활성화
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable()) // H2 Console을 위해 완전 비활성화
                )
                .authorizeHttpRequests(authz -> authz
                        // 공개 접근 허용
                        .requestMatchers("/", "/login", "/register").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // 정적 리소스 허용 (모든 사용자) - JavaScript 파일 포함
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/uploads/**", "/audio/**").permitAll()
                        .requestMatchers("/static/**", "/webjars/**").permitAll() // 추가된 정적 리소스 경로
                        .requestMatchers("/student/**", "/components/**").permitAll() // 키리보카 리소스 허용
                        .requestMatchers("/student/kiriboca/js/**").permitAll() // 키리보카 JS 파일 허용
                        .requestMatchers("/student/kiriboca/css/**").permitAll() // 키리보카 CSS 파일 허용
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**",
                                      "/img/**", "/images/**", "/static/**", "/webjars/**",
                                      "/student/kiriboca/css/**", "/student/kiriboca/js/**",
                                      "/uploads/**", "/audio/**", "/h2-console/**", "/api/coins/**").permitAll()


                        // 디버그 및 테스트 허용 (개발용)
                        .requestMatchers("/debug/**", "/init-test-data", "/check-users").permitAll()

                        // 관리자 전용 API 및 페이지 /api/coins
                        .requestMatchers("/api/coins/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 학부모 전용 API (필요시)
                        .requestMatchers("/api/parent/**").hasRole("PARENT")

                        // 학생 전용 API (필요시)
                        .requestMatchers("/api/student/**").hasRole("STUDENT")

                        // 사용자 API (로그인한 모든 사용자)
                        .requestMatchers("/api/user/**", "/api/learning/**").authenticated()
                        .requestMatchers("/learning/api/**").authenticated()
                        .requestMatchers("/api/sidebar/**").authenticated()
                        .requestMatchers("/api/coins/**").authenticated()
                        .requestMatchers("/dashboard").authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")  // 이메일을 username으로 받음
                        .passwordParameter("password")
                        .successHandler(successHandler())
                        .defaultSuccessUrl("/student/kiriboca/index", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false); // true에서 false로 변경
        configuration.setMaxAge(3600L); // 1시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}