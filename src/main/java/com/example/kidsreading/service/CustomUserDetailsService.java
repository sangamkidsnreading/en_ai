package com.example.kidsreading.service;

import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("로그인 시도: {}", username);

        // 이메일 또는 사용자명으로 사용자 조회
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });

        log.info("사용자 조회 성공: {} (ID: {})", user.getEmail(), user.getId());

        // 계정 상태 확인
        if (!user.getIsActive()) {
            log.error("비활성화된 계정입니다: {}", username);
            throw new UsernameNotFoundException("비활성화된 계정입니다.");
        }

        // 마지막 로그인 시간 업데이트 (비동기로 처리)
        updateLastLoginAsync(user);

        // UserDetails 객체 생성 및 반환
        return new CustomUserPrincipal(user);
    }

    /**
     * 마지막 로그인 시간 업데이트 (비동기)
     */
    private void updateLastLoginAsync(User user) {
        try {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            log.info("마지막 로그인 시간 업데이트: {} (ID: {})", user.getEmail(), user.getId());
        } catch (Exception e) {
            log.warn("마지막 로그인 시간 업데이트 실패: {}", e.getMessage());
        }
    }

    /**
     * Custom UserDetails 구현 클래스
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getIsActive();
        }

        // 추가 메서드들
        public String getEmail() {
            return user.getEmail();
        }

        public String getName() {
            return user.getName();
        }

        public User.Role getRole() {
            return user.getRole();
        }

        public Long getId() {
            return user.getId();
        }
    }
}