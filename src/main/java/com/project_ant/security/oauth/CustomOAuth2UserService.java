package com.project_ant.security.oauth;

import com.project_ant.domain.User;
import com.project_ant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        // Provider별 정보 추출
        String providerId = extractProviderId(provider, oAuth2User);
        String email = extractEmail(provider, oAuth2User);
        String name = extractName(provider, oAuth2User);
        String imageUrl = extractProfileImage(provider, oAuth2User);

        // ProviderId가 없으면 예외 발생 (필수값)
        if (providerId == null || providerId.isBlank()) {
            throw new OAuth2AuthenticationException("ProviderId not found from OAuth2 provider: " + provider);
        }

        // DB에 사용자 등록 or 갱신 (provider + providerId 기준)
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userRepository.save(new User(email, provider, providerId)));

        // 프로필 정보 업데이트
        if (name != null || imageUrl != null) {
            user.updateProfile(name, imageUrl);
            userRepository.save(user);
        }

        return new CustomOAuth2User(email, provider, providerId, oAuth2User.getAttributes());
    }

    private String extractEmail(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case "google" -> (String) oAuth2User.getAttributes().get("email");
            case "kakao" -> {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
                String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                // 빈 문자열을 null로 변환 (unique constraint 위반 방지)
                yield (email != null && !email.isBlank()) ? email : null;
            }
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                yield response != null ? (String) response.get("email") : null;
            }
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        };
    }

    private String extractProviderId(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case "google" -> (String) oAuth2User.getAttributes().get("sub");
            case "kakao" -> String.valueOf(oAuth2User.getAttributes().get("id"));
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                yield response != null ? (String) response.get("id") : null;
            }
            default -> null;
        };
    }

    private String extractName(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case "google" -> (String) oAuth2User.getAttributes().get("name");
            case "kakao" -> {
                Map<String, Object> properties = (Map<String, Object>) oAuth2User.getAttributes().get("properties");
                yield properties != null ? (String) properties.get("nickname") : null;
            }
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                yield response != null ? (String) response.get("name") : null;
            }
            default -> null;
        };
    }

    private String extractProfileImage(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case "google" -> (String) oAuth2User.getAttributes().get("picture");
            case "kakao" -> {
                Map<String, Object> properties = (Map<String, Object>) oAuth2User.getAttributes().get("properties");
                yield properties != null ? (String) properties.get("profile_image") : null;
            }
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                yield response != null ? (String) response.get("profile_image") : null;
            }
            default -> null;
        };
    }
}
