// ========== CustomUserDetailsService.java ==========
package com.example.kidsreading.service;

import com.example.kidsreading.entity.User;
import com.example.kidsreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("사용자 로그인 시도: " + username);

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> {
                    System.err.println("사용자를 찾을 수 없음: " + username);
                    return new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        System.out.println("사용자 발견: " + user.getEmail() + " (ID: " + user.getId() + ")");

        // 사용자 활성화 상태 확인
        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new org.springframework.security.authentication.BadCredentialsException("비승인 상태입니다. 관리자에게 문의 바랍니다.");
        }

        return createUserPrincipal(user);
    }

    private UserDetails createUserPrincipal(User user) {
        try {
            // 마지막 로그인 시간 업데이트 (비동기로 처리하여 로그인 성능에 영향을 주지 않도록)
            CompletableFuture.runAsync(() -> {
                try {
                    user.setLastLogin(LocalDateTime.now());
                    userRepository.save(user);
                    System.out.println("마지막 로그인 시간 업데이트 완료: " + user.getEmail() + " (ID: " + user.getId() + ")");
                } catch (Exception e) {
                    System.err.println("마지막 로그인 시간 업데이트 실패");
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            // 로그인 실패하지 않도록 예외 처리
        }

        return new CustomUserPrincipal(user);
    }

    @Data
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
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