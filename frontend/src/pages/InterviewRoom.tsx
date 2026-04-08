import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Clock, Send, Hash, Loader2 } from "lucide-react";
import Header from "@/components/Header";

const sampleQuestions = [
  "프로세스와 스레드의 차이점에 대해 설명해주세요.",
  "컨텍스트 스위칭이 발생하는 과정을 단계별로 설명해주세요.",
  "교착 상태(Deadlock)의 4가지 필요 조건을 설명해주세요.",
  "가상 메모리란 무엇이며, 페이지 폴트 처리 과정을 설명해주세요.",
  "뮤텍스와 세마포어의 차이점을 설명해주세요.",
];

const InterviewRoom = () => {
  const navigate = useNavigate();
  const [currentQ, setCurrentQ] = useState(0);
  const [answer, setAnswer] = useState("");
  const [timeLeft, setTimeLeft] = useState(180);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const totalQ = 5;

  useEffect(() => {
    if (timeLeft <= 0 || isAnalyzing) return;
    const t = setInterval(() => setTimeLeft((p) => p - 1), 1000);
    return () => clearInterval(t);
  }, [timeLeft, isAnalyzing]);

  const formatTime = (s: number) => {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m}:${sec.toString().padStart(2, "0")}`;
  };

  const handleSubmit = () => {
    if (!answer.trim()) return;
    setIsAnalyzing(true);
    setTimeout(() => {
      navigate("/feedback", { state: { answer, question: sampleQuestions[currentQ], questionNum: currentQ + 1 } });
    }, 2000);
  };

  const isFollowUp = currentQ > 0 && currentQ % 2 === 1;

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />

      {/* Status Bar */}
      <div className="fixed top-14 left-0 right-0 z-40 bg-background border-b border-border">
        <div className="container flex items-center justify-between h-11 text-sm">
          <div className="flex items-center gap-2 font-display font-medium">
            <Hash className="w-4 h-4 text-primary" />
            <span className="text-primary">Q{currentQ + 1}</span>
            <span className="text-muted-foreground">/ {totalQ}</span>
          </div>
          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-1.5">
              {Array.from({ length: totalQ }).map((_, i) => (
                <div
                  key={i}
                  className={`w-7 h-1.5 rounded-full transition-colors ${
                    i < currentQ
                      ? "bg-success"
                      : i === currentQ
                      ? "bg-primary"
                      : "bg-border"
                  }`}
                />
              ))}
            </div>
            <div className={`flex items-center gap-1.5 font-mono text-sm font-medium ${timeLeft < 30 ? "text-destructive" : "text-foreground"}`}>
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
            {!isAnalyzing ? (
              <motion.div
                key="question"
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -12 }}
                transition={{ duration: 0.3 }}
              >
                <div className="mb-6">
                  {isFollowUp && (
                    <span className="inline-block px-2 py-0.5 rounded-md bg-primary/10 text-primary text-xs font-medium mb-2">
                      Follow-up
                    </span>
                  )}
                  <h2 className="text-lg md:text-xl font-display font-semibold leading-relaxed text-foreground">
                    {sampleQuestions[currentQ]}
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
                    <span className="text-xs text-muted-foreground">
                      {answer.length}자
                    </span>
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
