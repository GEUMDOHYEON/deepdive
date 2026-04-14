# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

**DeepDive** — AI 기반 모의 면접 플랫폼 (풀스택)

- **Backend**: `backend/` — Spring Boot 4.0.3, Java 21, PostgreSQL
- **Frontend**: `frontend/` — React 18 + TypeScript, Vite, Tailwind CSS, shadcn/ui

## 명령어

### Backend (`backend/` 디렉토리에서 실행)

```bash
./gradlew bootRun        # 개발 서버 시작 (포트 8080)
./gradlew test           # JUnit 5 테스트 전체 실행
./gradlew test --tests "me.dohyeon.deepdive.domain.auth.*"  # 특정 테스트 실행
./gradlew build          # JAR 빌드
./gradlew clean          # 빌드 결과물 삭제
```

Swagger UI: http://localhost:8080/swagger-ui.html

### Frontend (`frontend/` 디렉토리에서 실행)

```bash
npm run dev      # 개발 서버 시작 (포트 3000)
npm run build    # 프로덕션 빌드
npm run lint     # ESLint 실행
npm test         # Vitest 테스트 실행
npm run test:watch  # watch 모드 테스트
```

## 아키텍처

### Backend — 도메인 레이어드 구조

```
me.dohyeon.deepdive
├── domain/
│   ├── auth/        # JWT 인증, Google OAuth2, 회원가입/로그인
│   ├── interview/   # 면접 세션, 질문 생성 (Spring AI / OpenAI)
│   └── member/      # 사용자 프로필 관리
└── global/
    ├── common/      # CommonResponse<T>, BaseEntity (created_at/updated_at)
    ├── config/      # Security, JPA, AI 설정
    ├── error/       # 전역 예외 핸들러, 에러 코드 enum
    └── security/
        ├── jwt/     # JWT 토큰 생성/검증 필터
        └── oauth2/  # OAuth2 성공 핸들러, 사용자 정보 로딩
```

각 도메인 패키지: `controller/`, `service/`, `repository/`, `entity/`, `dto/`

**데이터 흐름**: Controller → Service → Repository → Entity
**응답 형식**: 모든 API는 `CommonResponse<T> { success, data, error }` 로 래핑

### Frontend — 페이지 기반 구조

```
src/
├── pages/           # Index, InterviewRoom, Feedback, Report
├── components/      # Header, Footer, NavLink, ui/ (shadcn 컴포넌트)
├── hooks/           # use-mobile, use-toast
└── lib/             # utils.ts (cn 헬퍼)
```

**라우팅**: React Router v6 — `/`, `/interview`, `/feedback`, `/report`
**상태관리**: TanStack Query (서버 상태), React Hook Form + Zod (폼 검증)
**API 통신**: axios, 백엔드 기본 URL `http://localhost:8080`

## 코드 규칙

### Backend

- Entity에 `@Setter` 사용 금지 → `@Builder` 또는 정적 팩토리 메서드 사용
- DTO는 Java Record 사용, 이름은 `...Request` / `...Response`
- 비즈니스 예외 발생 시 반드시 커스텀 에러 코드 포함
- `spring.threads.virtual.enabled: true` 절대 변경 금지 (Java 21 가상 스레드)
- DB 테이블·컬럼명은 `snake_case` (PostgreSQL 표준)

### Frontend

- UI 컴포넌트는 `src/components/ui/`의 shadcn 컴포넌트 우선 재사용
- 스타일링은 Tailwind CSS 유틸리티 클래스 사용

## 환경 변수

Backend `backend/.env`:
```
DB_USERNAME=
DB_PASSWORD=
OPENAI_API_KEY=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
JWT_SECRET=
```

Frontend `frontend/.env` (필요 시):
```
VITE_API_BASE_URL=http://localhost:8080
```

## 핵심 도메인 모델 (ERD 요약)

- **Member**: 이메일/패스워드 로컬 로그인 + Google OAuth2 통합
- **InterviewSession**: 카테고리별 면접 세션 (운영체제, 네트워크, 데이터베이스 등), 상태: `IN_PROGRESS` / `COMPLETED`
- **InterviewQuestion**: 세션 내 순서 있는 질문, 꼬리 질문(`is_follow_up`) 지원
- **MemberAnswer**: 사용자 답변 텍스트 + 답변 소요 시간
- **AiFeedback**: OpenAI가 생성한 정확성/논리 점수, 피드백 코멘트, 모범 답안
- **RefreshToken**: JWT Refresh Token 관리 (14일 만료)
