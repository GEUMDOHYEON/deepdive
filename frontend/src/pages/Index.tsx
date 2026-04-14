import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Cpu, Database, Network, Binary, Shield, Layers, Shuffle, ArrowRight, Loader2, PlayCircle } from "lucide-react";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import { interviewApi, CATEGORY_MAP, InProgressSession } from "@/api/interview";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

const categories = [
  { id: "os", label: "운영체제", icon: Cpu, desc: "프로세스, 스레드, 메모리 관리" },
  { id: "db", label: "데이터베이스", icon: Database, desc: "SQL, 인덱싱, 트랜잭션" },
  { id: "network", label: "네트워크", icon: Network, desc: "TCP/IP, HTTP, 소켓" },
  { id: "ds", label: "자료구조", icon: Binary, desc: "트리, 그래프, 해시" },
  { id: "security", label: "보안", icon: Shield, desc: "암호화, 인증, CORS" },
  { id: "architecture", label: "소프트웨어 설계", icon: Layers, desc: "디자인 패턴, SOLID" },
];

const CATEGORY_KO: Record<string, string> = {
  OS: "운영체제",
  DATABASE: "데이터베이스",
  NETWORK: "네트워크",
  DATA_STRUCTURE: "자료구조",
  SECURITY: "보안",
  SOFTWARE_DESIGN: "소프트웨어 설계",
};

const container = {
  hidden: {},
  show: { transition: { staggerChildren: 0.05 } },
};

const item = {
  hidden: { opacity: 0, y: 12 },
  show: { opacity: 1, y: 0, transition: { duration: 0.3 } },
};

const Index = () => {
  const navigate = useNavigate();
  const [loadingCategory, setLoadingCategory] = useState<string | null>(null);
  const [inProgressSessions, setInProgressSessions] = useState<InProgressSession[]>([]);
  // 새 세션 시작 전 충돌 확인용: 선택한 카테고리 id 임시 저장
  const [pendingCategory, setPendingCategory] = useState<string | null>(null);

  useEffect(() => {
    interviewApi
      .getInProgressSessions()
      .then((res) => setInProgressSessions(res.data.data))
      .catch(() => {}); // 조회 실패는 조용히 무시
  }, []);

  const goToSession = (session: InProgressSession) => {
    navigate("/interview", {
      state: {
        sessionId: session.sessionId,
        questionId: session.currentQuestion.questionId,
        questionContent: session.currentQuestion.content,
        sequence: session.currentQuestion.sequence,
        isFollowUp: session.currentQuestion.followUp,
      },
    });
  };

  const startNewSession = async (categoryId: string) => {
    if (loadingCategory) return;
    setLoadingCategory(categoryId);
    try {
      const res = await interviewApi.startSession(CATEGORY_MAP[categoryId]);
      const { sessionId, firstQuestion } = res.data.data;
      // 기존 진행 중 세션 목록에서 새로 시작한 세션은 제거 (낙관적 업데이트)
      setInProgressSessions([]);
      navigate("/interview", {
        state: {
          sessionId,
          questionId: firstQuestion.questionId,
          questionContent: firstQuestion.content,
          sequence: firstQuestion.sequence,
          isFollowUp: false,
        },
      });
    } catch {
      // 401은 인터셉터가 처리
    } finally {
      setLoadingCategory(null);
    }
  };

  const handleCategoryClick = (categoryId: string) => {
    if (inProgressSessions.length > 0) {
      setPendingCategory(categoryId);
    } else {
      startNewSession(categoryId);
    }
  };

  const handleRandom = () => {
    const randomCat = categories[Math.floor(Math.random() * categories.length)];
    handleCategoryClick(randomCat.id);
  };

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />

      {/* Hero */}
      <section className="pt-28 pb-12">
        <div className="container text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <h1 className="text-3xl md:text-5xl font-display font-bold leading-tight mb-4 max-w-2xl mx-auto text-foreground">
              지식의 습득을 넘어 <span className="text-primary">인출</span>로.
            </h1>
            <p className="text-base text-muted-foreground max-w-lg mx-auto">
              AI 면접관과 함께 딥다이브 하세요. 꼬리 질문과 실시간 피드백으로
              진짜 실력을 확인합니다.
            </p>
          </motion.div>
        </div>
      </section>

      {/* 진행 중 세션 이어하기 배너 */}
      {inProgressSessions.length > 0 && (
        <section className="pb-2">
          <div className="container max-w-2xl">
            <div className="space-y-2">
              {inProgressSessions.map((session) => (
                <motion.div
                  key={session.sessionId}
                  initial={{ opacity: 0, y: -8 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="flex items-center justify-between p-4 rounded-xl bg-primary/5 border border-primary/20"
                >
                  <div className="flex items-center gap-3">
                    <PlayCircle className="w-5 h-5 text-primary shrink-0" />
                    <div>
                      <p className="text-sm font-medium text-foreground">
                        {CATEGORY_KO[session.category] ?? session.category} 면접 진행 중
                      </p>
                      <p className="text-xs text-muted-foreground mt-0.5 line-clamp-1">
                        Q{session.currentQuestion.sequence}. {session.currentQuestion.content}
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => goToSession(session)}
                    className="shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-primary text-primary-foreground text-xs font-medium hover:opacity-90 transition-opacity ml-3"
                  >
                    이어하기
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </motion.div>
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Categories */}
      <section className="py-10">
        <div className="container">
          <h2 className="text-lg font-display font-semibold mb-6 text-center text-foreground">
            카테고리 선택
          </h2>
          <motion.div
            className="grid grid-cols-2 md:grid-cols-3 gap-3 max-w-2xl mx-auto"
            variants={container}
            initial="hidden"
            whileInView="show"
            viewport={{ once: true }}
          >
            {categories.map((cat) => (
              <motion.button
                key={cat.id}
                variants={item}
                onClick={() => handleCategoryClick(cat.id)}
                disabled={!!loadingCategory}
                className="group flex flex-col items-start gap-2 p-4 rounded-xl bg-background border border-border hover:border-primary/50 hover:bg-secondary transition-all duration-200 text-left disabled:opacity-60 disabled:cursor-not-allowed"
              >
                {loadingCategory === cat.id ? (
                  <Loader2 className="w-5 h-5 text-primary animate-spin" />
                ) : (
                  <cat.icon className="w-5 h-5 text-primary" />
                )}
                <div>
                  <p className="font-display font-medium text-sm text-foreground">{cat.label}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{cat.desc}</p>
                </div>
              </motion.button>
            ))}
          </motion.div>
        </div>
      </section>

      {/* Quick Start */}
      <section className="py-10 pb-16">
        <div className="container flex items-center justify-center">
          <button
            onClick={handleRandom}
            disabled={!!loadingCategory}
            className="flex items-center gap-2 px-5 py-2.5 rounded-lg bg-primary text-primary-foreground font-medium text-sm hover:opacity-90 transition-opacity disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loadingCategory ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Shuffle className="w-4 h-4" />
            )}
            랜덤 면접 시작
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </section>

      <Footer />

      {/* 진행 중 세션 충돌 다이얼로그 */}
      <AlertDialog open={!!pendingCategory} onOpenChange={(open) => !open && setPendingCategory(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>진행 중인 면접이 있습니다</AlertDialogTitle>
            <AlertDialogDescription>
              완료하지 않은 면접 세션이 있습니다. 새 면접을 시작하면 이전 세션은 미완료 상태로 남습니다.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setPendingCategory(null)}>
              취소
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                goToSession(inProgressSessions[0]);
                setPendingCategory(null);
              }}
              className="bg-secondary text-secondary-foreground hover:bg-secondary/80"
            >
              이어하기
            </AlertDialogAction>
            <AlertDialogAction
              onClick={() => {
                const cat = pendingCategory!;
                setPendingCategory(null);
                startNewSession(cat);
              }}
            >
              새로 시작
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Index;
