package com.project_ant.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(length = 100)
    private String password;  // OAuth2만 사용하면 null

    @Column(length = 255)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;  // 각 플랫폼의 고유 사용자 ID

    @Column(length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "refreshTokenExpiryDate")
    private LocalDateTime refreshTokenExpiryDate;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public User(String email, String provider, String providerId) {
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void updateProfile(String name, String imageUrl) {
        if (name != null) {
            this.name = name;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime expiryDate) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryDate = expiryDate;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiryDate = null;
    }
}
