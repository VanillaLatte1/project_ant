package com.project_ant.controller;

import com.project_ant.domain.User;
import com.project_ant.dto.UserResponse;
import com.project_ant.dto.UserUpdateRequest;
import com.project_ant.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        user.updateProfile(request.getName(), request.getImageUrl());
        userRepository.save(user);

        log.info("프로필 수정 완료: userId={}", user.getId());

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        userRepository.delete(user);

        log.info("회원 탈퇴 완료: userId={}", user.getId());

        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = (String) authentication.getPrincipal();
        if (email == null) {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }
}
