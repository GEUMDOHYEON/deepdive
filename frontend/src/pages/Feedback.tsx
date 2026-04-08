import { useNavigate, useLocation } from "react-router-dom";
import { motion } from "framer-motion";
import { MessageSquare, ArrowRight, FileText } from "lucide-react";
import Header from "@/components/Header";

const Feedback = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const question = (location.state as any)?.question || "프로세스와 스레드의 차이점에 대해 설명해주세요.";
  const answer =
    (location.state as any)?.answer ||
    "프로세스는 독립적인 메모리 공간을 가지는 실행 단위이고, 스레드는 프로세스 내에서 메모리를 공유하며 실행되는 단위입니다. 컨텍스트 스위칭 비용이 스레드가 더 적습니다.";

  const keywords = ["프로세스", "스레드", "메모리", "컨텍스트 스위칭"];

  const highlightAnswer = (text: string) => {
    let result = text;
    keywords.forEach((kw) => {
      result = result.split(kw).join(
        `<mark class="bg-primary/15 text-primary px-0.5 rounded font-medium">${kw}</mark>`
      );
    });
    return result;
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
            {/* Original Answer */}
            <div className="mb-6 p-4 rounded-xl bg-secondary border border-border">
              <p className="text-xs text-muted-foreground mb-2 font-medium">내 답변</p>
              <p
                className="text-sm text-foreground leading-relaxed"
                dangerouslySetInnerHTML={{ __html: highlightAnswer(answer) }}
              />
            </div>

            {/* AI Feedback */}
            <div className="mb-6 p-4 rounded-xl border border-border bg-background">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center">
                  <MessageSquare className="w-3.5 h-3.5 text-primary" />
                </div>
                <span className="font-display font-medium text-sm text-primary">AI 면접관 피드백</span>
              </div>
              <p className="text-sm text-foreground leading-relaxed">
                좋은 답변입니다! 프로세스와 스레드의 핵심 차이를 잘 설명하셨습니다. 
                다만, <strong className="text-primary font-medium">컨텍스트 스위칭</strong>에 대해 좀 더 깊이 들어가 보겠습니다.
              </p>
            </div>

            {/* Follow-up Question */}
            <motion.div
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, duration: 0.4 }}
              className="p-5 rounded-xl bg-secondary border border-border mb-6"
            >
              <span className="inline-block px-2 py-0.5 rounded-md bg-primary/10 text-primary text-xs font-medium mb-2">
                꼬리 질문
              </span>
              <p className="text-base font-display font-medium leading-relaxed text-foreground">
                "방금 말씀하신 '<span className="text-primary">컨텍스트 스위칭</span>'에서 발생하는 
                오버헤드에 대해 더 자세히 설명해주실 수 있나요?"
              </p>
            </motion.div>

            {/* Actions */}
            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={() => navigate("/interview")}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-primary text-primary-foreground font-medium text-sm hover:opacity-90 transition-opacity"
              >
                <ArrowRight className="w-4 h-4" />
                꼬리 질문에 답변하기
              </button>
              <button
                onClick={() => navigate("/report")}
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
