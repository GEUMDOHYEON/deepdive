package me.dohyeon.deepdive.domain.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.interview.dto.AnswerSubmitRequest;
import me.dohyeon.deepdive.domain.interview.dto.AnswerSubmitResponse;
import me.dohyeon.deepdive.domain.interview.dto.InProgressSessionResponse;
import me.dohyeon.deepdive.domain.interview.dto.SessionResultResponse;
import me.dohyeon.deepdive.domain.interview.dto.SessionSummaryResponse;
import me.dohyeon.deepdive.domain.interview.dto.StartSessionRequest;
import me.dohyeon.deepdive.domain.interview.dto.StartSessionResponse;
import me.dohyeon.deepdive.domain.interview.service.InterviewSessionService;
import me.dohyeon.deepdive.global.common.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "면접", description = "면접 세션 관리 API")
@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

  private final InterviewSessionService interviewSessionService;

  @Operation(
      summary = "면접 세션 시작",
      description = "카테고리를 선택하여 새로운 면접 세션을 시작합니다. " +
          "세션 생성과 동시에 첫 번째 질문이 반환됩니다. 로그인이 필요합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "세션 생성 성공"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 카테고리"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @PostMapping("/sessions")
  @ResponseStatus(HttpStatus.CREATED)
  public CommonResponse<StartSessionResponse> startSession(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody StartSessionRequest request
  ) {
    return CommonResponse.ok(interviewSessionService.startSession(memberId, request));
  }

  @Operation(
      summary = "답변 제출 및 AI 평가",
      description = "면접 질문에 대한 답변을 제출합니다. AI가 답변을 평가하고 다음 질문(또는 꼬리질문)을 생성합니다. " +
          "5번째 답변 제출 시 세션이 완료되며 최종 점수가 산출됩니다. 로그인이 필요합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "답변 제출 및 평가 성공"),
      @ApiResponse(responseCode = "400", description = "이미 완료된 세션"),
      @ApiResponse(responseCode = "401", description = "인증 필요"),
      @ApiResponse(responseCode = "404", description = "세션 또는 질문을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "AI 평가 처리 실패")
  })
  @PostMapping("/sessions/{sessionId}/questions/{questionId}/answers")
  public CommonResponse<AnswerSubmitResponse> submitAnswer(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @PathVariable Long sessionId,
      @PathVariable Long questionId,
      @RequestBody AnswerSubmitRequest request
  ) {
    return CommonResponse.ok(
        interviewSessionService.submitAnswer(sessionId, questionId, memberId, request)
    );
  }

  @Operation(
      summary = "세션 최종 리포트 조회",
      description = "완료된 면접 세션의 모든 질문·답변·AI 피드백을 한데 모아 반환합니다. 로그인이 필요합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리포트 조회 성공"),
      @ApiResponse(responseCode = "400", description = "아직 완료되지 않은 세션"),
      @ApiResponse(responseCode = "401", description = "인증 필요"),
      @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @GetMapping("/sessions/{sessionId}/result")
  public CommonResponse<SessionResultResponse> getSessionResult(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @PathVariable Long sessionId
  ) {
    return CommonResponse.ok(interviewSessionService.getSessionReport(memberId, sessionId));
  }

  @Operation(
      summary = "면접 세션 히스토리 조회",
      description = "면접자의 모든 세션의 히스토리를 최신순으로 조회합니다. 로그인이 필요합니다."
  )
  @GetMapping("/sessions/history")
  public CommonResponse<List<SessionSummaryResponse>> getSessionHistory(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
  ) {
    return CommonResponse.ok(interviewSessionService.getSessionHistory(memberId));
  }

  @Operation(
      summary = "진행 중 세션 조회",
      description = "완료되지 않은 면접 세션과 현재 질문을 반환합니다. 로그인이 필요합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @GetMapping("/sessions/in-progress")
  public CommonResponse<List<InProgressSessionResponse>> getInProgressSessions(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
  ) {
    return CommonResponse.ok(interviewSessionService.getInProgressSessions(memberId));
  }
}
