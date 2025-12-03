package com.project_ant.security.oauth;

import com.project_ant.domain.User;
import com.project_ant.repository.UserRepository;
import com.project_ant.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.oauth.redirect-success-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // provider:providerId 형태로 사용자 식별
        String userKey = oAuth2User.getUserKey();

        // Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userKey);

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(userKey);

        // DB에서 사용자 조회 후 리프레시 토큰 저장
        userRepository.findByProviderAndProviderId(oAuth2User.getProvider(), oAuth2User.getProviderId())
                .ifPresent(user -> {
                    LocalDateTime expiryDate = LocalDateTime.now()
                            .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000);
                    user.updateRefreshToken(refreshToken, expiryDate);
                    userRepository.save(user);
                    log.debug("리프레시 토큰 저장 완료: userId={}", user.getId());
                });

        log.info("OAuth2 login success - provider: {}, providerId: {}",
                oAuth2User.getProvider(), oAuth2User.getProviderId());

        // 프론트엔드로 리다이렉트 (토큰 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
