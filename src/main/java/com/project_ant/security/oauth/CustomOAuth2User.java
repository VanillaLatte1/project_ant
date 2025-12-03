package com.project_ant.security.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private final String email;
    private final String provider;
    private final String providerId;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(String email, String provider, String providerId, Map<String, Object> attributes) {
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.attributes = attributes;
    }

    public String getEmail() {
        return email;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getUserKey() {
        return provider + ":" + providerId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return email != null ? email : getUserKey();
    }
}
