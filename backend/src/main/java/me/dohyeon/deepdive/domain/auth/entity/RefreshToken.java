package me.dohyeon.deepdive.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false, unique = true)
  private Long memberId;

  @Column(nullable = false, length = 512)
  private String token;

  @Column(name = "expiry_date", nullable = false)
  private LocalDateTime expiryDate;

  @Builder
  private RefreshToken(Long memberId, String token, LocalDateTime expiryDate) {
    this.memberId = memberId;
    this.token = token;
    this.expiryDate = expiryDate;
  }

  public static RefreshToken create(Long memberId, String token, LocalDateTime expiryDate) {
    return RefreshToken.builder()
        .memberId(memberId)
        .token(token)
        .expiryDate(expiryDate)
        .build();
  }

  /**
   * Refresh Token Rotation: 재발급 시 기존 레코드를 갱신합니다.
   */
  public void rotate(String newToken, LocalDateTime newExpiryDate) {
    this.token = newToken;
    this.expiryDate = newExpiryDate;
  }
}
