package me.dohyeon.deepdive.domain.interview.dto;

public record InProgressSessionResponse(
    Long sessionId,
    String category,
    QuestionResponse currentQuestion
) {
  public record QuestionResponse(
      Long questionId,
      String content,
      int sequence,
      boolean followUp
  ) {}
}
