package me.dohyeon.deepdive.domain.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.dohyeon.deepdive.domain.interview.dto.AiFeedbackTextResult;
import me.dohyeon.deepdive.domain.interview.dto.AiNextQuestionResult;
import me.dohyeon.deepdive.domain.interview.dto.AiScoreResult;
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
     * 답변의 점수·누락 키워드를 평가한다 (숫자+짧은 목록 → 빠름).
     *
     * <p>3-way 병렬 최적화의 첫 번째 호출.
     * evaluateScores / generateFeedbackText / generateNextQuestion 이 세 호출은
     * {@link InterviewSessionService}에서 {@code CompletableFuture.allOf()}로 동시 실행된다.</p>
     */
    public AiScoreResult evaluateScores(String questionContent, String userAnswer) {
        long t0 = System.currentTimeMillis();
        log.debug("[evaluateScores] 시작 thread={}", Thread.currentThread().getName());
        String systemPrompt = """
                너는 현업 10년 차 시니어 백엔드 개발자 출신의 엄격한 기술 면접관이야.
                아래의 [면접 질문]에 대한 [지원자 답변]을 분석하고, 점수와 누락 키워드만 평가해.

                [면접 질문]
                %s

                [지원자 답변]
                %s

                === 평가 기준 ===
                1. scoreAccuracy (정수 0~10): 기술적 정확성. 10점: 완전 정확, 0점: 전혀 모름.
                2. scoreLogic (정수 0~10): 논리적 전달력. 10점: 체계적·명확, 0점: 두서 없음.
                3. missingKeywords (문자열 배열): 답변에서 누락된 핵심 기술 용어·개념 목록. 완벽하면 빈 배열.

                === 응답 규칙 ===
                - 아래 JSON만 반환. 마크다운·설명 텍스트 금지.

                {"scoreAccuracy":<0~10>,"scoreLogic":<0~10>,"missingKeywords":["<키워드>"]}
                """.formatted(questionContent, userAnswer);

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user("점수를 평가하고 JSON으로 반환해.")
                .call()
                .content();

        AiScoreResult result = parseJson(raw, AiScoreResult.class, "점수 평가");
        log.debug("[evaluateScores] 완료 소요={}ms", System.currentTimeMillis() - t0);
        return result;
    }

    /**
     * 답변에 대한 피드백 코멘트와 모범 답안을 생성한다 (긴 텍스트 → 느림, 하지만 다른 두 호출과 병렬).
     */
    public AiFeedbackTextResult generateFeedbackText(String questionContent, String userAnswer) {
        long t0 = System.currentTimeMillis();
        log.debug("[generateFeedbackText] 시작 thread={}", Thread.currentThread().getName());
        String systemPrompt = """
                너는 현업 10년 차 시니어 백엔드 개발자 출신의 면접관이야.
                아래의 [면접 질문]에 대한 [지원자 답변]을 읽고 두 가지를 작성해.

                [면접 질문]
                %s

                [지원자 답변]
                %s

                1. feedbackComment: 답변의 강점과 약점을 1~2문장으로 한국어 작성.
                2. idealAnswer: 모범 답안을 1~2문장으로 한국어 작성 (고득점 수준).

                === 응답 규칙 ===
                - 아래 JSON만 반환. 마크다운·설명 텍스트 금지.

                {"feedbackComment":"<한국어 피드백>","idealAnswer":"<한국어 모범 답안>"}
                """.formatted(questionContent, userAnswer);

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user("피드백과 모범 답안을 작성하고 JSON으로 반환해.")
                .call()
                .content();

        AiFeedbackTextResult result = parseJson(raw, AiFeedbackTextResult.class, "피드백 텍스트");
        log.debug("[generateFeedbackText] 완료 소요={}ms", System.currentTimeMillis() - t0);
        return result;
    }

    /**
     * 다음 면접 질문을 생성한다 (꼬리질문 또는 신규 심화질문).
     *
     * <p>질문과 답변을 바탕으로 AI가 자체적으로 답변 수준을 판단해
     * 꼬리질문(followUp=true) 또는 새 심화질문(followUp=false)을 결정한다.</p>
     */
    public AiNextQuestionResult generateNextQuestion(String questionContent, String userAnswer) {
        long t0 = System.currentTimeMillis();
        log.debug("[generateNextQuestion] 시작 thread={}", Thread.currentThread().getName());
        String systemPrompt = """
                너는 현업 10년 차 시니어 백엔드 개발자 출신의 기술 면접관이야.
                아래의 [면접 질문]에 대한 [지원자 답변]을 읽고, 다음 면접 질문 1개를 생성해.

                [면접 질문]
                %s

                [지원자 답변]
                %s

                === 다음 질문 생성 규칙 ===
                - 답변이 피상적이거나 핵심을 놓쳤다면 → 해당 약점을 파고드는 '꼬리 질문' 생성, followUp=true
                - 답변이 충분히 정확하다면 → 같은 기술 카테고리 내의 '새로운 심화 질문' 생성, followUp=false
                - 질문은 부연 설명 없이 질문 텍스트만 작성.

                === 응답 규칙 ===
                - 반드시 아래 JSON 구조와 정확히 일치하는 순수 JSON 문자열만 반환해.
                - 마크다운 코드 블록(```), 설명 텍스트, 공백 줄은 절대 포함하지 마.

                {
                  "nextQuestionContent": "<다음 질문 텍스트>",
                  "followUp": <true 또는 false>
                }
                """.formatted(questionContent, userAnswer);

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user("다음 질문을 생성하고 JSON으로 반환해.")
                .call()
                .content();

        AiNextQuestionResult result = parseJson(raw, AiNextQuestionResult.class, "다음 질문 생성");
        log.debug("[generateNextQuestion] 완료 소요={}ms", System.currentTimeMillis() - t0);
        return result;
    }

    /**
     * AI 응답 문자열을 지정 타입으로 파싱한다.
     *
     * <ol>
     *   <li>앞뒤 공백 제거 후 직접 역직렬화 시도.</li>
     *   <li>실패 시 마크다운 코드 블록 제거 후 재시도.</li>
     *   <li>두 번째 시도도 실패하면 {@link ErrorCode#AI_EVALUATION_FAILED} 예외 발생.</li>
     * </ol>
     */
    private <T> T parseJson(String rawResponse, Class<T> type, String context) {
        String json = rawResponse.strip();

        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException firstException) {
            log.warn("AI 응답 1차 파싱 실패 [{}], 코드 블록 제거 후 재시도. raw={}", context, json);
        }

        String cleaned = json
                .replaceAll("(?s)^```(json)?\\s*", "")
                .replaceAll("```\\s*$", "")
                .strip();

        try {
            return objectMapper.readValue(cleaned, type);
        } catch (JsonProcessingException secondException) {
            log.error("AI 응답 2차 파싱 실패 [{}]. cleaned={}", context, cleaned, secondException);
            throw new BusinessException(ErrorCode.AI_EVALUATION_FAILED);
        }
    }
}
