package com.project_ant.dto;

import com.project_ant.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String imageUrl;
    private String provider;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .imageUrl(user.getImageUrl())
                .provider(user.getProvider())
                .createdAt(user.getCreateAt())
                .build();
    }
}
