# 프로젝트: DeepDive

Java 21 가상 스레드(Virtual Threads), Spring Boot 4.0.3, PostgreSQL을 사용하는 AI 기반 모의 면접 백엔드 애플리케이션입니다.

## 코드 스타일

- Java: 21 LTS 버전 사용, 가상 스레드 최적화 지향
- Entity: `@Setter` 사용 금지, `@Builder` 또는 정적 팩토리 메서드 사용
- DTO: Java Record 클래스 사용 (명명 규칙: `...Request`, `...Response`)
- 인증: JWT 기반 인증 체계 및 Spring Security 사용
- 응답: 모든 API는 공통 응답 객체(`CommonResponse<T>`)로 래핑하여 반환

## 명령어

- `./gradlew bootRun`: 개발 서버 시작 (포트 8080)
- `./gradlew test`: JUnit 5 단위 테스트 실행
- `./gradlew build`: 프로젝트 빌드 및 JAR 생성
- `./gradlew clean`: 빌드 결과물 삭제
- `./gradlew generateSwagger`: Swagger API 명세서 생성 (적용 시)

## 아키텍처 (도메인형 구조)

- `me.dohyeon.deepdive.domain.{domain_name}`: 각 도메인별 Controller, Service, Repository, Entity, DTO 모음
- `me.dohyeon.deepdive.global.config`: Security, JPA, AI 관련 설정
- `me.dohyeon.deepdive.global.common`: 공통 응답, 예외 처리, BaseEntity
- `me.dohyeon.deepdive.global.error`: 전역 예외 핸들러 및 에러 코드 정의

## 중요 사항

- 가상 스레드: `application.yml`의 `spring.threads.virtual.enabled: true` 설정을 절대 변경하지 마세요.
- 데이터베이스: 모든 테이블 및 컬럼명은 PostgreSQL 표준인 `snake_case`를 사용합니다.
- 예외 처리: 비즈니스 로직 에러 시 반드시 커스텀 에러 코드를 포함하여 예외를 발생시키세요.
- AI 연동: 외부 LLM API 호출 시 타임아웃 처리와 재시도(Retry) 전략을 고려하세요.
- 보안: .env파일과 `application.yml`의 DB 비번이나 API 키는 절대 깃허브에 커밋하지 마세요 (환경 변수 사용 권장).
