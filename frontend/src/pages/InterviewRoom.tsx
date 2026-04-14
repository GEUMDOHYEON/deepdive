import { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Clock, Send, Hash, Loader2 } from "lucide-react";
import Header from "@/components/Header";
import { interviewApi } from "@/api/interview";

interface LocationState {
  sessionId: number;
  questionId: number;
  questionContent: string;
  sequence: number;
  isFollowUp: boolean;
}

const TOTAL_Q = 5;

const InterviewRoom = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | null;

  const [answer, setAnswer] = useState("");
  const [timeLeft, setTimeLeft] = useState(180);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const startTimeRef = useRef(Date.now());

  // 세션 정보 없이 접근 시 홈으로
  useEffect(() => {
    if (!state?.sessionId) {
      navigate("/");
    }
  }, [state, navigate]);

  useEffect(() => {
    startTimeRef.current = Date.now();
    setTimeLeft(180);
  }, [state?.questionId]);

  useEffect(() => {
    if (timeLeft <= 0 || isSubmitting) return;
    const t = setInterval(() => setTimeLeft((p) => p - 1), 1000);
    return () => clearInterval(t);
  }, [timeLeft, isSubmitting]);

  const formatTime = (s: number) => {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m}:${sec.toString().padStart(2, "0")}`;
  };

  const handleSubmit = async () => {
    if (!answer.trim() || !state) return;
    setIsSubmitting(true);

    const processingTime = Math.floor((Date.now() - startTimeRef.current) / 1000);

    try {
      const res = await interviewApi.submitAnswer(
        state.sessionId,
        state.questionId,
        answer.trim(),
        processingTime
      );
      const data = res.data.data;

      if (data.sessionCompleted) {
        navigate("/report", { state: { sessionId: state.sessionId } });
      } else {
        navigate("/feedback", {
          state: {
            sessionId: state.sessionId,
            question: state.questionContent,
            answer: answer.trim(),
            feedback: data.feedback,
            nextQuestion: data.nextQuestion,
            sequence: state.sequence,
          },
        });
      }
    } catch {
      setIsSubmitting(false);
    }
  };

  if (!state?.sessionId) return null;

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />

      {/* Status Bar */}
      <div className="fixed top-14 left-0 right-0 z-40 bg-background border-b border-border">
        <div className="container flex items-center justify-between h-11 text-sm">
          <div className="flex items-center gap-2 font-display font-medium">
            <Hash className="w-4 h-4 text-primary" />
            <span className="text-primary">Q{state.sequence}</span>
            <span className="text-muted-foreground">/ {TOTAL_Q}</span>
          </div>
          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-1.5">
              {Array.from({ length: TOTAL_Q }).map((_, i) => (
                <div
                  key={i}
                  className={`w-7 h-1.5 rounded-full transition-colors ${
                    i < state.sequence - 1
                      ? "bg-success"
                      : i === state.sequence - 1
                      ? "bg-primary"
                      : "bg-border"
                  }`}
                />
              ))}
            </div>
            <div
              className={`flex items-center gap-1.5 font-mono text-sm font-medium ${
                timeLeft < 30 ? "text-destructive" : "text-foreground"
              }`}
            >
              <Clock className="w-4 h-4" />
              {formatTime(timeLeft)}
            </div>
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="flex-1 pt-32 pb-12">
        <div className="container max-w-2xl">
          <AnimatePresence mode="wait">
            {!isSubmitting ? (
              <motion.div
                key="question"
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -12 }}
                transition={{ duration: 0.3 }}
              >
                <div className="mb-6">
                  {state.isFollowUp && (
                    <span className="inline-block px-2 py-0.5 rounded-md bg-primary/10 text-primary text-xs font-medium mb-2">
                      Follow-up
                    </span>
                  )}
                  <h2 className="text-lg md:text-xl font-display font-semibold leading-relaxed text-foreground">
                    {state.questionContent}
                  </h2>
                </div>

                <div className="relative">
                  <textarea
                    value={answer}
                    onChange={(e) => setAnswer(e.target.value)}
                    placeholder="답변을 입력하세요..."
                    className="w-full min-h-[200px] p-4 rounded-xl bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary outline-none resize-none text-foreground placeholder:text-muted-foreground transition-all text-sm"
                  />
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-xs text-muted-foreground">{answer.length}자</span>
                    <button
                      onClick={handleSubmit}
                      disabled={!answer.trim()}
                      className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:opacity-90 transition-opacity"
                    >
                      <Send className="w-4 h-4" />
                      답변 전송
                    </button>
                  </div>
                </div>
              </motion.div>
            ) : (
              <motion.div
                key="analyzing"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="flex flex-col items-center justify-center py-20 gap-4"
              >
                <Loader2 className="w-8 h-8 text-primary animate-spin" />
                <div className="text-center">
                  <p className="font-display font-medium text-base mb-1 text-foreground">
                    AI 면접관이 답변을 분석 중입니다...
                  </p>
                  <p className="text-sm text-muted-foreground">
                    키워드 추출 및 정확성 검증 진행 중
                  </p>
                </div>
                <div className="w-full max-w-sm space-y-2 mt-2">
                  {[80, 60, 90].map((w, i) => (
                    <div
                      key={i}
                      className="h-2.5 rounded-full bg-secondary animate-pulse"
                      style={{ width: `${w}%` }}
                    />
                  ))}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </main>
    </div>
  );
};

export default InterviewRoom;
