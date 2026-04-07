package me.dohyeon.deepdive.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.dohyeon.deepdive.global.common.entity.BaseEntity;

@Entity
@Table(name = "member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "social_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String email;

  @Column(length = 255)
  private String password;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(name = "social_id")
  private String socialId;

  @Column(name = "social_provider", length = 50)
  private String socialProvider;

  @Builder
  private Member(String email, String password, String nickname, String socialId,
      String socialProvider) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.socialId = socialId;
    this.socialProvider = socialProvider;
  }

  /**
   * 이메일/비밀번호 로컬 가입용 생성 메서드. socialId, socialProvider는 null로 설정된다.
   */
  public static Member createLocal(String email, String encodedPassword, String nickname) {
    return Member.builder()
        .email(email)
        .password(encodedPassword)
        .nickname(nickname)
        .build();
  }

  /**
   * 구글 OAuth2 가입용 생성 메서드. password는 null로 설정된다.
   */
  public static Member createGoogle(String email, String nickname, String socialId) {
    return Member.builder()
        .email(email)
        .nickname(nickname)
        .socialId(socialId)
        .socialProvider("GOOGLE")
        .build();
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}
