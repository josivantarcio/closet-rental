import { api } from "../api";
import type { LoginRequest, LoginResponse } from "@/types";

export const authService = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const res = await api.post<LoginResponse>("/v1/auth/login", data);
    return res.data;
  },

  logout: async () => {
    try {
      await api.post("/v1/auth/logout");
    } catch {
      // ignora falha no logout remoto
    }
  },
};
