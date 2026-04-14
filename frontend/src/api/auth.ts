import api from "@/lib/api";

export interface MemberResponse {
  id: number;
  email: string;
  nickname: string;
}

export const authApi = {
  signup: (email: string, password: string, nickname: string) =>
    api.post<{ success: boolean; data: MemberResponse }>("/api/v1/auth/signup", {
      email,
      password,
      nickname,
    }),

  login: (email: string, password: string) =>
    api.post<{ success: boolean; data: MemberResponse }>("/api/v1/auth/login", {
      email,
      password,
    }),

  logout: () => api.post("/api/v1/auth/logout"),
};
