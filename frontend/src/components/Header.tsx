import { Link } from "react-router-dom";
import { User, Waves } from "lucide-react";

const Header = () => {
  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-background border-b border-border">
      <div className="container flex items-center justify-between h-14">
        <Link to="/" className="flex items-center gap-2 group">
          <Waves className="w-5 h-5 text-primary" />
          <span className="font-display text-lg font-semibold text-foreground">
            DeepDive
          </span>
        </Link>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-secondary">
            <div className="w-2 h-2 rounded-full bg-success" />
            <span className="text-xs text-secondary-foreground">접속 중</span>
          </div>
          <button className="flex items-center justify-center w-8 h-8 rounded-full bg-secondary hover:bg-surface-hover transition-colors">
            <User className="w-4 h-4 text-secondary-foreground" />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
