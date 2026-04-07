package me.dohyeon.deepdive.global.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String socialId;
        String email;
        String nickname;

        if ("KAKAO".equals(provider)) {
            socialId = String.valueOf(attributes.get("id"));
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            @SuppressWarnings("unchecked")
            Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
            nickname = profile != null ? (String) profile.get("nickname") : null;
        } else { // GOOGLE
            socialId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
        }

        return new CustomOAuth2User(socialId, provider, email, nickname, attributes);
    }
}
