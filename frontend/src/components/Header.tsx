import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Waves, LogOut, User } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

const Header = () => {
  const navigate = useNavigate();
  const { isLoggedIn, user, logout } = useAuth();
  const [loggingOut, setLoggingOut] = useState(false);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await logout();
      navigate("/login");
    } finally {
      setLoggingOut(false);
    }
  };

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-background border-b border-border">
      <div className="container flex items-center justify-between h-14">
        <Link to="/" className="flex items-center gap-2 group">
          <Waves className="w-5 h-5 text-primary" />
          <span className="font-display text-lg font-semibold text-foreground">DeepDive</span>
        </Link>

        <div className="flex items-center gap-3">
          {isLoggedIn ? (
            <>
              <div className="hidden sm:flex items-center gap-2 px-3 py-1 rounded-full bg-secondary">
                <User className="w-3.5 h-3.5 text-muted-foreground" />
                <span className="text-xs text-secondary-foreground">{user?.nickname}</span>
              </div>
              <button
                onClick={handleLogout}
                disabled={loggingOut}
                className="flex items-center justify-center w-8 h-8 rounded-full bg-secondary hover:bg-surface-hover transition-colors disabled:opacity-50"
                title="로그아웃"
              >
                <LogOut className="w-4 h-4 text-secondary-foreground" />
              </button>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="text-sm text-muted-foreground hover:text-foreground transition-colors"
              >
                로그인
              </Link>
              <Link
                to="/signup"
                className="px-3 py-1.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity"
              >
                회원가입
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
