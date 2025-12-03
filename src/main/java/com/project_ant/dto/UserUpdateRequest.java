package com.project_ant.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 100, message = "이름은 100자 이내여야 합니다")
    private String name;

    @Size(max = 500, message = "이미지 URL은 500자 이내여야 합니다")
    private String imageUrl;
}
