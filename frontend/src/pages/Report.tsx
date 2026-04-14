import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { motion } from "framer-motion";
import { RotateCcw, Home, CheckCircle2, XCircle, MessageSquare, Loader2 } from "lucide-react";
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer } from "recharts";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import { interviewApi, SessionResultResponse } from "@/api/interview";

interface LocationState {
  sessionId: number;
}

const CATEGORY_KO: Record<string, string> = {
  OS: "운영체제",
  DATABASE: "데이터베이스",
  NETWORK: "네트워크",
  DATA_STRUCTURE: "자료구조",
  SECURITY: "보안",
  SOFTWARE_DESIGN: "소프트웨어 설계",
};

const Report = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | null;

  const [result, setResult] = useState<SessionResultResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!state?.sessionId) {
      navigate("/");
      return;
    }
    interviewApi
      .getSessionResult(state.sessionId)
      .then((res) => setResult(res.data.data))
      .catch(() => setError("리포트를 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [state, navigate]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Loader2 className="w-8 h-8 text-primary animate-spin" />
      </div>
    );
  }

  if (error || !result) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-background gap-3">
        <p className="text-sm text-muted-foreground">{error || "결과를 불러올 수 없습니다."}</p>
        <button
          onClick={() => navigate("/")}
          className="text-sm text-primary hover:underline"
        >
          홈으로 돌아가기
        </button>
      </div>
    );
  }

  // 레이더 차트 데이터 — QnA 평균으로 계산
  const avgAccuracy = Math.round(
    result.qnaList.reduce((s, q) => s + q.scoreAccuracy, 0) / result.qnaList.length
  );
  const avgLogic = Math.round(
    result.qnaList.reduce((s, q) => s + q.scoreLogic, 0) / result.qnaList.length
  );

  const radarData = [
    { subject: "기술 정확성", score: avgAccuracy, fullMark: 100 },
    { subject: "논리 구조", score: avgLogic, fullMark: 100 },
    { subject: "종합 점수", score: result.totalScore ?? 0, fullMark: 100 },
  ];

  // 모든 누락 키워드 수집 (중복 제거)
  const allMissingKeywords = [
    ...new Set(result.qnaList.flatMap((q) => q.missingKeywords ?? [])),
  ];

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />

      <main className="flex-1 pt-24 pb-16">
        <div className="container max-w-3xl">
          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
            <div className="text-center mb-10">
              <h1 className="text-2xl font-display font-bold text-foreground">정밀 분석 리포트</h1>
              <p className="text-sm text-muted-foreground mt-1">
                {CATEGORY_KO[result.category] ?? result.category} 세션 결과
              </p>
            </div>

            {/* Score Dashboard */}
            <div className="grid md:grid-cols-2 gap-4 mb-10">
              <div className="p-5 rounded-xl bg-background border border-border">
                <h3 className="font-display font-medium text-sm mb-3 text-foreground">종합 역량 분석</h3>
                <ResponsiveContainer width="100%" height={220}>
                  <RadarChart data={radarData}>
                    <PolarGrid stroke="hsl(0 0% 90%)" />
                    <PolarAngleAxis dataKey="subject" tick={{ fill: "hsl(0 0% 55%)", fontSize: 11 }} />
                    <PolarRadiusAxis angle={90} domain={[0, 100]} tick={{ fill: "hsl(0 0% 55%)", fontSize: 10 }} />
                    <Radar dataKey="score" stroke="hsl(160 60% 38%)" fill="hsl(160 60% 38%)" fillOpacity={0.15} strokeWidth={2} />
                  </RadarChart>
                </ResponsiveContainer>
              </div>

              <div className="p-5 rounded-xl bg-background border border-border flex flex-col">
                <h3 className="font-display font-medium text-sm mb-3 text-foreground">점수 요약</h3>
                <div className="space-y-3 flex-1">
                  {radarData.map((d) => (
                    <div key={d.subject}>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-muted-foreground">{d.subject}</span>
                        <span className="font-medium text-foreground">{d.score}점</span>
                      </div>
                      <div className="h-1.5 rounded-full bg-secondary overflow-hidden">
                        <motion.div
                          className="h-full rounded-full bg-primary"
                          initial={{ width: 0 }}
                          animate={{ width: `${d.score}%` }}
                          transition={{ duration: 0.8, delay: 0.2 }}
                        />
                      </div>
                    </div>
                  ))}
                  <div className="pt-3 border-t border-border mt-auto">
                    <div className="flex justify-between text-sm">
                      <span className="font-display font-semibold text-foreground">총점</span>
                      <span className="font-display font-bold text-primary">{result.totalScore}점</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* QnA Timeline */}
            <div className="mb-10">
              <h3 className="font-display font-medium text-base mb-4 text-foreground">세션 타임라인</h3>
              <div className="space-y-3">
                {result.qnaList.map((qna, i) => (
                  <motion.div
                    key={qna.questionId}
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: i * 0.06 }}
                    className="rounded-xl border border-border overflow-hidden"
                  >
                    {/* 질문 */}
                    <div className="flex gap-3 p-3 bg-secondary">
                      <MessageSquare className="w-4 h-4 text-primary mt-0.5 shrink-0" />
                      <p className="text-sm text-foreground">{qna.questionContent}</p>
                    </div>
                    {/* 답변 */}
                    <div className="flex gap-3 p-3 bg-background">
                      <div className="pt-0.5">
                        {(qna.scoreAccuracy + qna.scoreLogic) / 2 >= 70 ? (
                          <CheckCircle2 className="w-4 h-4 text-success" />
                        ) : (
                          <XCircle className="w-4 h-4 text-warning" />
                        )}
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-0.5">
                          <span className="text-xs font-medium text-muted-foreground">내 답변</span>
                          <span
                            className={`text-xs font-medium px-1.5 py-0.5 rounded-full ${
                              (qna.scoreAccuracy + qna.scoreLogic) / 2 >= 70
                                ? "bg-success/10 text-success"
                                : "bg-warning/10 text-warning"
                            }`}
                          >
                            정확성 {qna.scoreAccuracy} / 논리 {qna.scoreLogic}
                          </span>
                        </div>
                        <p className="text-sm text-foreground leading-relaxed">{qna.userAnswer}</p>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>

            {/* Missing Keywords */}
            {allMissingKeywords.length > 0 && (
              <div className="mb-10 p-5 rounded-xl bg-background border border-border">
                <h3 className="font-display font-medium text-sm mb-3 text-foreground">놓친 핵심 키워드</h3>
                <div className="flex flex-wrap gap-2">
                  {allMissingKeywords.map((kw) => (
                    <span
                      key={kw}
                      className="px-2.5 py-1 rounded-lg bg-destructive/8 text-destructive text-xs font-medium border border-destructive/15"
                    >
                      {kw}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={() => navigate("/")}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-primary text-primary-foreground font-medium text-sm hover:opacity-90 transition-opacity"
              >
                <RotateCcw className="w-4 h-4" />
                새 면접 시작
              </button>
              <button
                onClick={() => navigate("/")}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-secondary text-secondary-foreground font-medium text-sm hover:bg-surface-hover transition-colors border border-border"
              >
                <Home className="w-4 h-4" />
                홈으로 돌아가기
              </button>
            </div>
          </motion.div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default Report;
