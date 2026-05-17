import api from "@/lib/api";
import { MemberResponse } from "@/api/auth";

export const memberApi = {
  getMe: () =>
    api.get<{ success: boolean; data: MemberResponse }>("/api/v1/members/me"),

  updateNickname: (nickname: string) =>
    api.patch<{ success: boolean; data: MemberResponse }>("/api/v1/members/me", { nickname }),

  deleteMe: () => api.delete("/api/v1/members/me"),
};
