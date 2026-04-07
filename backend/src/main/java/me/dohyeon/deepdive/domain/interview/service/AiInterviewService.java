package me.dohyeon.deepdive.domain.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.dohyeon.deepdive.domain.interview.dto.AiEvaluationResponse;
import me.dohyeon.deepdive.domain.interview.entity.InterviewCategory;
import me.dohyeon.deepdive.global.error.BusinessException;
import me.dohyeon.deepdive.global.error.ErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiInterviewService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiInterviewService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String generateFirstQuestion(InterviewCategory category) {
        return chatClient.prompt()
                .system("""
                        너는 10년 차 시니어 백엔드 개발자 면접관이야.
                        사용자가 선택한 기술 카테고리(%s)에 대해, 주니어 개발자의 기본기를 확인할 수 있는 핵심 면접 질문을 딱 1개만 생성해 줘.
                        부연 설명, 인사말, 특수기호 없이 오직 '질문 텍스트'만 깔끔하게 반환해.
                        """.formatted(category.getDescription()))
                .user("질문을 생성해줘.")
                .call()
                .content();
    }

    /**
     * 지원자의 답변을 평가하고, 다음 질문(꼬리질문 또는 신규질문)을 함께 생성한다.
     *
     * <p>System Prompt 전략: AI가 평가 + 다음 질문 생성을 단일 API 호출로 처리하도록
     * 역할, 평가 기준, 응답 포맷을 엄격하게 명시한다. 마크다운 없이 순수 JSON만 반환하도록
     * 강제하고, 파싱 실패 시 코드 블록 제거 후 재시도한다.</p>
     *
     * @param questionContent 면접관이 출제한 질문 텍스트
     * @param userAnswer      지원자가 제출한 답변 텍스트
     * @return AI가 평가한 {@link AiEvaluationResponse} 객체
     */
    public AiEvaluationResponse evaluateAnswer(String questionContent, String userAnswer) {
        String systemPrompt = buildEvaluationSystemPrompt(questionContent, userAnswer);

        String rawResponse = chatClient.prompt()
                .system(systemPrompt)
                .user("위 기준에 따라 답변을 평가하고 JSON으로 반환해.")
                .call()
                .content();

        return parseEvaluationResponse(rawResponse);
    }

    /**
     * System Prompt를 생성한다.
     *
     * <ul>
     *   <li>역할 정의: 10년 차 엄격한 기술 면접관</li>
     *   <li>입력값: questionContent, userAnswer를 프롬프트 내에 직접 삽입</li>
     *   <li>평가 항목 7가지를 명확한 지침으로 제시</li>
     *   <li>꼬리질문 vs 신규질문 분기 기준을 명시 (followUp 필드와 연동)</li>
     *   <li>응답 포맷: JSON 스키마를 인라인으로 제공하고, 마크다운/부연 텍스트 금지</li>
     * </ul>
     */
    private String buildEvaluationSystemPrompt(String questionContent, String userAnswer) {
        return """
                너는 현업 10년 차 시니어 백엔드 개발자 출신의 엄격한 기술 면접관이야.
                아래의 [면접 질문]에 대한 [지원자 답변]을 꼼꼼히 분석하고, 아래 7가지 항목을 평가해.

                [면접 질문]
                %s

                [지원자 답변]
                %s

                === 평가 기준 ===
                1. scoreAccuracy (정수 0~10): 기술적 정확성 점수.
                   - 사실 오류, 개념 혼동, 핵심 내용 누락 시 감점.
                   - 10점: 완전 정확, 0점: 전혀 모름.

                2. scoreLogic (정수 0~10): 논리적 전달력 점수.
                   - 답변 구조의 명확성, 예시 활용, 인과관계 설명 여부를 평가.
                   - 10점: 체계적·명확, 0점: 두서 없음.

                3. feedbackComment (문자열): 답변의 강점과 약점을 구체적으로 2~3문장으로 한국어 작성.
                   - 좋은 점과 부족한 점을 모두 언급할 것.

                4. missingKeywords (문자열 배열): 답변에서 누락된 핵심 기술 용어·개념 목록.
                   - 완벽한 답변이었다면 빈 배열([])로 반환.

                5. idealAnswer (문자열): 이 질문에 대한 모범 답안을 2~4문장으로 한국어 작성.
                   - 면접에서 고득점을 받을 수 있는 수준으로 작성.

                6. nextQuestionContent (문자열): 다음 질문 텍스트 1개 생성.
                   - 규칙: scoreAccuracy < 6 이거나 답변이 피상적이라면 → 해당 약점을 파고드는 '꼬리 질문' 생성.
                   - 규칙: scoreAccuracy >= 6 이고 답변이 충분하다면 → 같은 기술 카테고리 내의 '새로운 심화 질문' 생성.
                   - 부연 설명 없이 질문 텍스트만 작성.

                7. followUp (불리언): 꼬리 질문이면 true, 새로운 심화 질문이면 false.

                === 응답 규칙 ===
                - 반드시 아래 JSON 구조와 정확히 일치하는 순수 JSON 문자열만 반환해.
                - 마크다운 코드 블록(```), 설명 텍스트, 공백 줄은 절대 포함하지 마.
                - 모든 문자열 값은 큰따옴표(")로 감싸고, JSON을 탈출 처리해.

                {
                  "scoreAccuracy": <0~10 정수>,
                  "scoreLogic": <0~10 정수>,
                  "feedbackComment": "<한국어 피드백, 2~3문장>",
                  "missingKeywords": ["<키워드1>", "<키워드2>"],
                  "idealAnswer": "<한국어 모범 답안, 2~4문장>",
                  "nextQuestionContent": "<다음 질문 텍스트>",
                  "followUp": <true 또는 false>
                }
                """.formatted(questionContent, userAnswer);
    }

    /**
     * AI 응답 문자열을 {@link AiEvaluationResponse}로 파싱한다.
     *
     * <p>파싱 전략:</p>
     * <ol>
     *   <li>앞뒤 공백 제거 후 직접 역직렬화 시도.</li>
     *   <li>실패 시 마크다운 코드 블록(```json ... ```) 제거 후 재시도.
     *       — AI가 프롬프트 지시를 무시하고 코드 블록을 붙이는 경우를 방어한다.</li>
     *   <li>두 번째 시도도 실패하면 {@link ErrorCode#AI_EVALUATION_FAILED} 예외 발생.</li>
     * </ol>
     */
    private AiEvaluationResponse parseEvaluationResponse(String rawResponse) {
        String json = rawResponse.strip();

        try {
            return objectMapper.readValue(json, AiEvaluationResponse.class);
        } catch (JsonProcessingException firstException) {
            log.warn("AI 응답 1차 파싱 실패, 코드 블록 제거 후 재시도. raw={}", json);
        }

        // 마크다운 코드 블록 제거 후 재시도
        String cleaned = json
                .replaceAll("(?s)^```(json)?\\s*", "")
                .replaceAll("```\\s*$", "")
                .strip();

        try {
            return objectMapper.readValue(cleaned, AiEvaluationResponse.class);
        } catch (JsonProcessingException secondException) {
            log.error("AI 응답 2차 파싱 실패. cleaned={}", cleaned, secondException);
            throw new BusinessException(ErrorCode.AI_EVALUATION_FAILED);
        }
    }
}
