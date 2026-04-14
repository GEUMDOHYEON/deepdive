import { useNavigate, useLocation } from "react-router-dom";
import { motion } from "framer-motion";
import { MessageSquare, ArrowRight, FileText } from "lucide-react";
import Header from "@/components/Header";
import { AnswerSubmitResponse } from "@/api/interview";

interface LocationState {
  sessionId: number;
  question: string;
  answer: string;
  feedback: AnswerSubmitResponse["feedback"];
  nextQuestion: AnswerSubmitResponse["nextQuestion"];
  sequence: number;
}

const Feedback = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | null;

  if (!state) {
    navigate("/");
    return null;
  }

  const { sessionId, question, answer, feedback, nextQuestion, sequence } = state;

  const highlightKeywords = (text: string, keywords: string[]) => {
    if (!keywords?.length) return text;
    let result = text;
    keywords.forEach((kw) => {
      result = result.split(kw).join(
        `<mark class="bg-primary/15 text-primary px-0.5 rounded font-medium">${kw}</mark>`
      );
    });
    return result;
  };

  const goToNextQuestion = () => {
    if (!nextQuestion) return;
    navigate("/interview", {
      state: {
        sessionId,
        questionId: nextQuestion.questionId,
        questionContent: nextQuestion.content,
        sequence: nextQuestion.sequence,
        isFollowUp: nextQuestion.followUp,
      },
    });
  };

  const endSession = () => {
    navigate("/report", { state: { sessionId } });
  };

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />

      <main className="flex-1 pt-24 pb-16">
        <div className="container max-w-2xl">
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
          >
            {/* 질문 */}
            <div className="mb-4 p-4 rounded-xl bg-secondary border border-border">
              <p className="text-xs text-muted-foreground mb-1.5 font-medium">Q{sequence}. 질문</p>
              <p className="text-sm text-foreground leading-relaxed">{question}</p>
            </div>

            {/* 내 답변 (키워드 하이라이트) */}
            <div className="mb-6 p-4 rounded-xl bg-secondary border border-border">
              <p className="text-xs text-muted-foreground mb-2 font-medium">내 답변</p>
              <p
                className="text-sm text-foreground leading-relaxed"
                dangerouslySetInnerHTML={{
                  __html: highlightKeywords(
                    answer,
                    feedback.missingKeywords ?? []
                  ),
                }}
              />
            </div>

            {/* AI 피드백 */}
            <div className="mb-6 p-4 rounded-xl border border-border bg-background">
              <div className="flex items-center gap-2 mb-3">
                <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center">
                  <MessageSquare className="w-3.5 h-3.5 text-primary" />
                </div>
                <span className="font-display font-medium text-sm text-primary">AI 면접관 피드백</span>
                <div className="ml-auto flex gap-2">
                  <span className="text-xs px-2 py-0.5 rounded-full bg-secondary text-muted-foreground">
                    정확성 {feedback.scoreAccuracy}점
                  </span>
                  <span className="text-xs px-2 py-0.5 rounded-full bg-secondary text-muted-foreground">
                    논리 {feedback.scoreLogic}점
                  </span>
                </div>
              </div>
              <p className="text-sm text-foreground leading-relaxed mb-3">
                {feedback.feedbackComment}
              </p>

              {feedback.missingKeywords?.length > 0 && (
                <div className="mb-3">
                  <p className="text-xs font-medium text-muted-foreground mb-1.5">놓친 키워드</p>
                  <div className="flex flex-wrap gap-1.5">
                    {feedback.missingKeywords.map((kw) => (
                      <span
                        key={kw}
                        className="px-2 py-0.5 rounded-md bg-destructive/8 text-destructive text-xs font-medium border border-destructive/15"
                      >
                        {kw}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              <div className="pt-3 border-t border-border">
                <p className="text-xs font-medium text-muted-foreground mb-1">모범 답안</p>
                <p className="text-sm text-foreground leading-relaxed">{feedback.idealAnswer}</p>
              </div>
            </div>

            {/* 꼬리 질문 */}
            {nextQuestion && (
              <motion.div
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2, duration: 0.4 }}
                className="p-5 rounded-xl bg-secondary border border-border mb-6"
              >
                <span className="inline-block px-2 py-0.5 rounded-md bg-primary/10 text-primary text-xs font-medium mb-2">
                  {nextQuestion.followUp ? "꼬리 질문" : `Q${nextQuestion.sequence}`}
                </span>
                <p className="text-base font-display font-medium leading-relaxed text-foreground">
                  "{nextQuestion.content}"
                </p>
              </motion.div>
            )}

            {/* Actions */}
            <div className="flex flex-col sm:flex-row gap-3">
              {nextQuestion && (
                <button
                  onClick={goToNextQuestion}
                  className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-primary text-primary-foreground font-medium text-sm hover:opacity-90 transition-opacity"
                >
                  <ArrowRight className="w-4 h-4" />
                  {nextQuestion.followUp ? "꼬리 질문에 답변하기" : "다음 질문 답변하기"}
                </button>
              )}
              <button
                onClick={endSession}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-secondary text-secondary-foreground font-medium text-sm hover:bg-surface-hover transition-colors border border-border"
              >
                <FileText className="w-4 h-4" />
                세션 종료 · 리포트 보기
              </button>
            </div>
          </motion.div>
        </div>
      </main>
    </div>
  );
};

export default Feedback;
