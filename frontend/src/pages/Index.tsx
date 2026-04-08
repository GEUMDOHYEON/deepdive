import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Cpu, Database, Network, Binary, Shield, Layers, Shuffle, RotateCcw, ArrowRight } from "lucide-react";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

const categories = [
  { id: "os", label: "운영체제", icon: Cpu, desc: "프로세스, 스레드, 메모리 관리" },
  { id: "db", label: "데이터베이스", icon: Database, desc: "SQL, 인덱싱, 트랜잭션" },
  { id: "network", label: "네트워크", icon: Network, desc: "TCP/IP, HTTP, 소켓" },
  { id: "ds", label: "자료구조", icon: Binary, desc: "트리, 그래프, 해시" },
  { id: "security", label: "보안", icon: Shield, desc: "암호화, 인증, CORS" },
  { id: "architecture", label: "소프트웨어 설계", icon: Layers, desc: "디자인 패턴, SOLID" },
];

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
                onClick={() => navigate("/interview")}
                className="group flex flex-col items-start gap-2 p-4 rounded-xl bg-background border border-border hover:border-primary/50 hover:bg-secondary transition-all duration-200 text-left"
              >
                <cat.icon className="w-5 h-5 text-primary" />
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
        <div className="container flex flex-col sm:flex-row items-center justify-center gap-3">
          <button
            onClick={() => navigate("/interview")}
            className="flex items-center gap-2 px-5 py-2.5 rounded-lg bg-primary text-primary-foreground font-medium text-sm hover:opacity-90 transition-opacity"
          >
            <Shuffle className="w-4 h-4" />
            랜덤 면접 시작
            <ArrowRight className="w-4 h-4" />
          </button>
          <button
            onClick={() => navigate("/interview")}
            className="flex items-center gap-2 px-5 py-2.5 rounded-lg bg-secondary text-secondary-foreground font-medium text-sm hover:bg-surface-hover transition-colors border border-border"
          >
            <RotateCcw className="w-4 h-4" />
            최근 오답 기반 시작
          </button>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default Index;
