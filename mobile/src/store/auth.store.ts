import { create } from "zustand";
import * as SecureStore from "expo-secure-store";
import type { Role } from "@/types";

interface AuthState {
  token: string | null;
  username: string | null;
  roles: string[];
  tenantId: string | null;
  isAuthenticated: boolean;

  setAuth: (token: string, username: string, roles: string[], tenantId?: string) => Promise<void>;
  clearAuth: () => Promise<void>;
  loadFromStorage: () => Promise<void>;
  hasRole: (role: Role) => boolean;
  isCliente: () => boolean;
  isAdmin: () => boolean;
  isAtendente: () => boolean;
  isSuperAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: null,
  username: null,
  roles: [],
  tenantId: null,
  isAuthenticated: false,

  setAuth: async (token, username, roles, tenantId) => {
    await SecureStore.setItemAsync("access_token", token);
    await SecureStore.setItemAsync("auth_username", username);
    await SecureStore.setItemAsync("auth_roles", JSON.stringify(roles));
    if (tenantId) await SecureStore.setItemAsync("auth_tenant", tenantId);
    set({ token, username, roles, tenantId: tenantId ?? null, isAuthenticated: true });
  },

  clearAuth: async () => {
    await SecureStore.deleteItemAsync("access_token");
    await SecureStore.deleteItemAsync("refresh_token");
    await SecureStore.deleteItemAsync("auth_username");
    await SecureStore.deleteItemAsync("auth_roles");
    await SecureStore.deleteItemAsync("auth_tenant");
    set({ token: null, username: null, roles: [], tenantId: null, isAuthenticated: false });
  },

  loadFromStorage: async () => {
    const token = await SecureStore.getItemAsync("access_token");
    const username = await SecureStore.getItemAsync("auth_username");
    const rolesStr = await SecureStore.getItemAsync("auth_roles");
    const tenantId = await SecureStore.getItemAsync("auth_tenant");
    if (token && username) {
      const roles = rolesStr ? JSON.parse(rolesStr) : [];
      set({ token, username, roles, tenantId, isAuthenticated: true });
    }
  },

  hasRole: (role) => get().roles.includes(`ROLE_${role}`),
  isCliente: () => get().roles.includes("ROLE_CLIENTE"),
  isAdmin: () => get().roles.includes("ROLE_ADMIN") || get().roles.includes("ROLE_SUPER_ADMIN"),
  isAtendente: () => get().roles.includes("ROLE_ATENDENTE"),
  isSuperAdmin: () => get().roles.includes("ROLE_SUPER_ADMIN"),
}));
