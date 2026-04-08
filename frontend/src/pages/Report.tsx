import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { RotateCcw, Home, CheckCircle2, XCircle, MessageSquare } from "lucide-react";
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer } from "recharts";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

const radarData = [
  { subject: "기술 정확성", score: 78, fullMark: 100 },
  { subject: "논리 구조", score: 85, fullMark: 100 },
  { subject: "핵심 키워드", score: 60, fullMark: 100 },
  { subject: "깊이", score: 72, fullMark: 100 },
  { subject: "표현력", score: 88, fullMark: 100 },
];

const timeline = [
  { type: "question" as const, content: "프로세스와 스레드의 차이점에 대해 설명해주세요." },
  { type: "answer" as const, content: "프로세스는 독립적인 메모리 공간을 가지는 실행 단위이고, 스레드는 프로세스 내에서 메모리를 공유하며 실행되는 단위입니다.", score: 78 },
  { type: "followup" as const, content: "컨텍스트 스위칭에서 발생하는 오버헤드에 대해 더 자세히 설명해주실 수 있나요?" },
  { type: "answer" as const, content: "컨텍스트 스위칭 시 PCB에 현재 상태를 저장하고, 새로운 프로세스의 PCB를 로드하는 과정에서 캐시 무효화와 TLB 플러시가 발생합니다.", score: 85 },
];

const missingKeywords = ["PCB", "레지스터 저장", "커널 모드 전환", "캐시 미스", "TLB", "IPC 비용"];

const Report = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />

      <main className="flex-1 pt-24 pb-16">
        <div className="container max-w-3xl">
          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
            <div className="text-center mb-10">
              <h1 className="text-2xl font-display font-bold text-foreground">정밀 분석 리포트</h1>
              <p className="text-sm text-muted-foreground mt-1">세션 결과를 확인하세요</p>
            </div>

            {/* Score Dashboard */}
            <div className="grid md:grid-cols-2 gap-4 mb-10">
              <div className="p-5 rounded-xl bg-background border border-border">
                <h3 className="font-display font-medium text-sm mb-3 text-foreground">종합 역량 분석</h3>
                <ResponsiveContainer width="100%" height={250}>
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
                </div>
              </div>
            </div>

            {/* Timeline */}
            <div className="mb-10">
              <h3 className="font-display font-medium text-base mb-4 text-foreground">세션 타임라인</h3>
              <div className="space-y-3">
                {timeline.map((item, i) => (
                  <motion.div
                    key={i}
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: i * 0.08 }}
                    className={`flex gap-3 p-3 rounded-xl border ${
                      item.type === "answer"
                        ? "bg-background border-border"
                        : item.type === "followup"
                        ? "bg-primary/5 border-primary/15"
                        : "bg-secondary border-border"
                    }`}
                  >
                    <div className="pt-0.5">
                      {item.type === "question" ? (
                        <MessageSquare className="w-4 h-4 text-primary" />
                      ) : item.type === "followup" ? (
                        <MessageSquare className="w-4 h-4 text-primary" />
                      ) : (item.score ?? 0) >= 80 ? (
                        <CheckCircle2 className="w-4 h-4 text-success" />
                      ) : (
                        <XCircle className="w-4 h-4 text-warning" />
                      )}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-0.5">
                        <span className="text-xs font-medium text-muted-foreground">
                          {item.type === "question" ? "질문" : item.type === "followup" ? "꼬리 질문" : "내 답변"}
                        </span>
                        {item.type === "answer" && (
                          <span className={`text-xs font-medium px-1.5 py-0.5 rounded-full ${
                            (item.score ?? 0) >= 80 ? "bg-success/10 text-success" : "bg-warning/10 text-warning"
                          }`}>
                            {item.score}점
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-foreground leading-relaxed">{item.content}</p>
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>

            {/* Missing Keywords */}
            <div className="mb-10 p-5 rounded-xl bg-background border border-border">
              <h3 className="font-display font-medium text-sm mb-3 text-foreground">놓친 핵심 키워드</h3>
              <div className="flex flex-wrap gap-2">
                {missingKeywords.map((kw) => (
                  <span key={kw} className="px-2.5 py-1 rounded-lg bg-destructive/8 text-destructive text-xs font-medium border border-destructive/15">
                    {kw}
                  </span>
                ))}
              </div>
            </div>

            {/* Actions */}
            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={() => navigate("/interview")}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-primary text-primary-foreground font-medium text-sm hover:opacity-90 transition-opacity"
              >
                <RotateCcw className="w-4 h-4" />
                오답 질문 다시 시도
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
