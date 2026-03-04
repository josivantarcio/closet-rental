import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { LoginResponse } from "@/types";

interface AuthUser {
  username: string;
  roles: string[];
  expiresIn: number;
}

interface AuthState {
  token: string | null;
  user: AuthUser | null;
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
          user: { username: data.username, roles: data.roles, expiresIn: data.expiresIn },
          isAuthenticated: true,
        });
        // Sincroniza com localStorage para o interceptor do axios
        localStorage.setItem("token", data.token);
        if (data.refreshToken) {
          localStorage.setItem("refreshToken", data.refreshToken);
        }
      },

      logout: () => {
        set({ token: null, user: null, isAuthenticated: false });
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
      },

      hasRole: (role) => {
        const roles = get().user?.roles ?? [];
        return roles.includes(`ROLE_${role}`) || roles.includes(role);
      },
    }),
    { name: "atelier-auth" }
  )
);
