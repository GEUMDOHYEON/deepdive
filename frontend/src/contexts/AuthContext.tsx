import { createContext, useContext, useState, useCallback, ReactNode } from "react";
import { authApi, MemberResponse } from "@/api/auth";

interface AuthContextValue {
  user: MemberResponse | null;
  isLoggedIn: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, nickname: string) => Promise<void>;
  logout: () => Promise<void>;
  setUser: (user: MemberResponse | null) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<MemberResponse | null>(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (email: string, password: string) => {
    const res = await authApi.login(email, password);
    const member = res.data.data;
    setUser(member);
    localStorage.setItem("user", JSON.stringify(member));
  }, []);

  const signup = useCallback(async (email: string, password: string, nickname: string) => {
    const res = await authApi.signup(email, password, nickname);
    const member = res.data.data;
    setUser(member);
    localStorage.setItem("user", JSON.stringify(member));
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    setUser(null);
    localStorage.removeItem("user");
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoggedIn: !!user, login, signup, logout, setUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};
