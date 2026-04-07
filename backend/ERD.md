# DeepDive Entity Relationship Diagram (ERD)

## DB 환경: PostgreSQL (snake_case 적용)

### 1. Member (사용자)

- `id`: BigInt (PK, Auto Increment)
- `email`: String (Varchar(50), Unique, Not Null)  /* 로컬 로그인 아이디 겸용 */
- `password`: String (Varchar(255), Nullable)      /* 구글 가입 유저는 null 허용 */
- `nickname`: String (Varchar(20), Not Null)
- `social_id`: String (Varchar(255), Unique, Nullable) /* 로컬 가입 유저는 null 허용 */
- `social_provider`: String (Varchar(50), Nullable)    /* 'GOOGLE' 또는 null */
- `created_at`: Timestamp (BaseEntity 상속)
- `updated_at`: Timestamp (BaseEntity 상속)

### 2. InterviewSession (면접 세션)

- `id`: BigInt (PK)
- `member_id`: BigInt (FK -> Member.id)
- `category`: String (Enum/String: 운영체제, 보안, 데이터베이스, 자료구조, 네트워크, 소프트웨어 설계)
- `status`: String (Enum: IN_PROGRESS, COMPLETED)
- `total_score`: Integer (면접 종료 후 합산된 총점)
- `created_at`: Timestamp

### 3. InterviewQuestion (인터뷰 질문)

- `id`: BigInt (PK)
- `session_id`: BigInt (FK -> InterviewSession.id)
- `member_id`: BigInt (FK -> Member.id)
- `content`: Text (질문 내용)
- `sequence`: Integer (세션 내 질문 순서)
- `is_follow_up`: Boolean (꼬리 질문 여부)

### 4. MemberAnswer (회원 답변)

- `id`: BigInt (PK)
- `question_id`: BigInt (FK -> InterviewQuestion.id)
- `session_id`: BigInt (FK -> InterviewSession.id)
- `member_id`: BigInt (FK -> Member.id)
- `content`: Text (사용자가 입력한 답변 텍스트)
- `processing_time`: Integer (답변에 소요된 시간/초)
- `created_at`: Timestamp (답변 제출 시간)

### 5. AiFeedback (AI 분석 리포트)

- `id`: BigInt (PK)
- `answer_id`: BigInt (FK -> MemberAnswer.id)
- `question_id`: BigInt (FK -> InterviewQuestion.id)
- `session_id`: BigInt (FK -> InterviewSession.id)
- `member_id`: BigInt (FK -> Member.id)
- `score_accuracy`: Integer (기술적 정확성 점수)
- `score_logic`: Integer (논리 구조 점수)
- `feedback_comment`: Text (AI의 종합 개선 코멘트)
- `missing_keywords`: Text (답변에서 누락된 핵심 키워드 리스트)
- `ideal_answer`: Text (AI가 제안하는 모범 답안)

### 6. RefreshToken (인증 토큰 관리)

- `id`: BigInt (PK, Auto Increment)
- `member_id`: BigInt (FK -> Member.id)
- `token`: String (Varchar(255), Unique, Not Null) /* 리프레쉬 토큰 값 */
- `expiry_date`: Timestamp (Not Null) /* 토큰 만료 일시 */
- `created_at`: Timestamp (BaseEntity 상속)
- `updated_at`: Timestamp (BaseEntity 상속)