package me.dohyeon.deepdive.domain.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.auth.dto.TokenPair;
import me.dohyeon.deepdive.domain.auth.entity.RefreshToken;
import me.dohyeon.deepdive.domain.auth.repository.RefreshTokenRepository;
import me.dohyeon.deepdive.global.error.BusinessException;
import me.dohyeon.deepdive.global.error.ErrorCode;
import me.dohyeon.deepdive.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  /**
   * AT + RT를 신규 발급합니다. 기존 RT가 존재하면 rotate(갱신), 없으면 신규 저장합니다.
   */
  public TokenPair issueTokens(Long memberId) {
    String accessToken = jwtProvider.generateAccessToken(memberId);
    String refreshToken = jwtProvider.generateRefreshToken(memberId);
    LocalDateTime expiryDate = LocalDateTime.now()
        .plusSeconds(jwtProvider.getRefreshTokenExpiration() / 1000);

    refreshTokenRepository.findByMemberId(memberId)
        .ifPresentOrElse(
            rt -> rt.rotate(refreshToken, expiryDate),
            () -> refreshTokenRepository.save(
                RefreshToken.create(memberId, refreshToken, expiryDate))
        );
 
    return new TokenPair(accessToken, refreshToken);
  }

  /**
   * RT를 검증하고 새로운 AT를 발급합니다. 검증 3단계: 1. JWT 서명/만료 검증 2. token type == "refresh" 확인 3. DB 저장값과 일치 여부
   * 확인 (불일치 시 토큰 탈취로 간주하고 RT 전체 삭제)
   */
  public String refresh(String refreshToken) {
    // 1. 서명/만료 검증
    if (!jwtProvider.validateToken(refreshToken)) {
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    // 2. type 클레임 확인
    Claims claims = jwtProvider.getClaims(refreshToken);
    if (!"refresh".equals(claims.get("type"))) {
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    Long memberId = jwtProvider.getMemberId(refreshToken);

    // 3. DB 저장값과 비교
    RefreshToken stored = refreshTokenRepository.findByMemberId(memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

    if (!stored.getToken().equals(refreshToken)) {
      // 토큰 재사용 공격 감지: 해당 회원의 RT를 즉시 무효화
      refreshTokenRepository.deleteByMemberId(memberId);
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    return jwtProvider.generateAccessToken(memberId);
  }

  /**
   * 로그아웃 시 RT 삭제
   */
  public void revokeRefreshToken(Long memberId) {
    refreshTokenRepository.deleteByMemberId(memberId);
  }
}
