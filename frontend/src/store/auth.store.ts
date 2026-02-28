import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { LoginResponse } from "@/types";

interface AuthState {
  token: string | null;
  user: Omit<LoginResponse, "token"> | null;
  isAuthenticated: boolean;
  login: (data: LoginResponse) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAuthenticated: false,

      login: (data) => {
        set({
          token: data.token,
          user: { username: data.username, roles: data.roles },
          isAuthenticated: true,
        });
        // Sincroniza com localStorage para o interceptor do axios
        localStorage.setItem("token", data.token);
      },

      logout: () => {
        set({ token: null, user: null, isAuthenticated: false });
        localStorage.removeItem("token");
      },

      hasRole: (role) => {
        const roles = get().user?.roles ?? [];
        return roles.includes(`ROLE_${role}`) || roles.includes(role);
      },
    }),
    { name: "atelier-auth" }
  )
);
