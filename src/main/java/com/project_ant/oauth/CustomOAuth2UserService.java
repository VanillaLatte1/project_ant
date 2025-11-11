package com.project_ant.oauth;

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

//    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        String email = switch (provider) {
            case "google" -> (String) oAuth2User.getAttributes().get("email");
            case "kakao" -> ((Map<String, Object>) oAuth2User.getAttributes().get("kakao_account")).get("email").toString();
            case "naver" -> ((Map<String, Object>) ((Map<String, Object>) oAuth2User.getAttributes().get("response"))).get("email").toString();
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        };

//        // DB에 사용자 등록 or 갱신
//        userRepository.findByEmail(email).orElseGet(() ->
//                userRepository.save(new User(email, provider))
//        );

        return new CustomOAuth2User(email, oAuth2User.getAttributes());
    }
}
