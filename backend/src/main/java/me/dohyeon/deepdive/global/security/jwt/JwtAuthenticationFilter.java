package me.dohyeon.deepdive.global.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtProvider.extractTokenFromCookie(request, "accessToken");

        // AT가 없거나 만료/유효하지 않으면 SecurityContext를 설정하지 않고 그냥 통과.
        // Spring Security가 인증이 필요한 엔드포인트에서 자연스럽게 401을 반환하고,
        // 프론트엔드 인터셉터가 이를 감지해 /api/v1/auth/refresh를 호출합니다.
        if (token != null && jwtProvider.validateToken(token)) {
            Claims claims = jwtProvider.getClaims(token);
            if ("access".equals(claims.get("type"))) {
                Long memberId = jwtProvider.getMemberId(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(memberId, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
