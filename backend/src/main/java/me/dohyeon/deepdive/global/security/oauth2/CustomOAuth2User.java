package me.dohyeon.deepdive.global.security.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final String socialId;
    private final String provider;
    private final String email;
    private final String nickname;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(String socialId, String provider, String email, String nickname,
                            Map<String, Object> attributes) {
        this.socialId = socialId;
        this.provider = provider;
        this.email = email;
        this.nickname = nickname;
        this.attributes = attributes;
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
        return socialId;
    }
}
