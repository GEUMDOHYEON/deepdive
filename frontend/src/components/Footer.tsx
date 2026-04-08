import { Github, Waves } from "lucide-react";

const Footer = () => {
  return (
    <footer className="border-t border-border py-6 mt-auto">
      <div className="container flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-muted-foreground">
          <Waves className="w-4 h-4 text-primary" />
          <span className="text-sm">DeepDive — AI 기술 면접 연습 플랫폼</span>
        </div>
        <a
          href="https://github.com"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <Github className="w-4 h-4" />
          GitHub
        </a>
      </div>
    </footer>
  );
};

export default Footer;
