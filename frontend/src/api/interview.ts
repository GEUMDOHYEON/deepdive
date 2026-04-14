import api from "@/lib/api";

// 프론트 카테고리 id → 백엔드 enum 매핑
export const CATEGORY_MAP: Record<string, string> = {
  os: "OS",
  db: "DATABASE",
  network: "NETWORK",
  ds: "DATA_STRUCTURE",
  security: "SECURITY",
  architecture: "SOFTWARE_DESIGN",
};

export interface StartSessionResponse {
  sessionId: number;
  category: string;
  status: string;
  firstQuestion: {
    questionId: number;
    content: string;
    sequence: number;
  };
}

export interface AnswerSubmitResponse {
  sessionCompleted: boolean;
  totalScore: number | null;
  feedback: {
    feedbackId: number;
    scoreAccuracy: number;
    scoreLogic: number;
    feedbackComment: string;
    missingKeywords: string[];
    idealAnswer: string;
  };
  nextQuestion: {
    questionId: number;
    content: string;
    sequence: number;
    followUp: boolean;
  } | null;
}

export interface QnaResult {
  questionId: number;
  questionContent: string;
  userAnswer: string;
  scoreAccuracy: number;
  scoreLogic: number;
  feedbackComment: string;
  missingKeywords: string[];
  idealAnswer: string;
}

export interface SessionResultResponse {
  sessionId: number;
  category: string;
  totalScore: number;
  status: string;
  qnaList: QnaResult[];
}

export const interviewApi = {
  startSession: (category: string) =>
    api.post<{ success: boolean; data: StartSessionResponse }>("/api/v1/interviews/sessions", {
      category,
    }),

  submitAnswer: (sessionId: number, questionId: number, content: string, processingTime: number) =>
    api.post<{ success: boolean; data: AnswerSubmitResponse }>(
      `/api/v1/interviews/sessions/${sessionId}/questions/${questionId}/answers`,
      { content, processingTime }
    ),

  getSessionResult: (sessionId: number) =>
    api.get<{ success: boolean; data: SessionResultResponse }>(
      `/api/v1/interviews/sessions/${sessionId}/result`
    ),
};
