package com.project_ant.security.jwt;

import com.project_ant.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null) {
                String userKey = jwtTokenProvider.validateAndGetSubject(token);

                if (userKey != null) {
                    authenticateUser(userKey);
                }
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private void authenticateUser(String userKey) {
        String[] parts = userKey.split(":", 2);

        if (parts.length != 2) {
            log.warn("잘못된 userKey 형식: {}", userKey);
            return;
        }

        String provider = parts[0];
        String providerId = parts[1];

        userRepository.findByProviderAndProviderId(provider, providerId)
                .ifPresentOrElse(
                        user -> {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                    );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("사용자 인증 완료: {}", user.getEmail());
                        },
                        () -> log.warn("사용자를 찾을 수 없음: provider={}, providerId={}", provider, providerId)
                );
    }
}
