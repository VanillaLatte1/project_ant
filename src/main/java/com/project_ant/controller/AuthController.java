package com.project_ant.controller;

import com.project_ant.domain.User;
import com.project_ant.dto.RefreshTokenRequest;
import com.project_ant.dto.TokenResponse;
import com.project_ant.repository.UserRepository;
import com.project_ant.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. 리프레시 토큰 검증 및 userKey 추출
        String userKey = jwtTokenProvider.validateRefreshTokenAndGetUserKey(refreshToken);
        if (userKey == null) {
            log.warn("유효하지 않은 리프레시 토큰");
            return ResponseEntity.status(401).build();
        }

        // 2. DB에서 리프레시 토큰으로 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);
        if (user == null) {
            log.warn("리프레시 토큰에 해당하는 사용자 없음");
            return ResponseEntity.status(401).build();
        }

        // 3. 리프레시 토큰 만료 시간 확인
        if (user.getRefreshTokenExpiryDate() == null ||
                user.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("리프레시 토큰 만료됨: userId={}", user.getId());
            user.clearRefreshToken();
            userRepository.save(user);
            return ResponseEntity.status(401).build();
        }

        // 4. 새 토큰 발급
        String newUserKey = user.getProvider() + ":" + user.getProviderId();
        String newAccessToken = jwtTokenProvider.generateAccessToken(newUserKey);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(newUserKey);

        // 5. 새 리프레시 토큰 저장 (Refresh Token Rotation)
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000);
        user.updateRefreshToken(newRefreshToken, expiryDate);
        userRepository.save(user);

        log.info("토큰 갱신 성공: userId={}", user.getId());

        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 리프레시 토큰으로 사용자 조회 후 토큰 삭제
        userRepository.findByRefreshToken(refreshToken).ifPresent(user -> {
            user.clearRefreshToken();
            userRepository.save(user);
            log.info("로그아웃 완료: userId={}", user.getId());
        });

        return ResponseEntity.ok().build();
    }
}
