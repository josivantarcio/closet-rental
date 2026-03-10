import axios from "axios";
import * as SecureStore from "expo-secure-store";

const DEFAULT_API_URL = process.env.EXPO_PUBLIC_API_URL ?? "http://10.0.2.2:8080";

export const SERVER_URL_KEY = "server_url";

export async function getApiUrl(): Promise<string> {
  const saved = await SecureStore.getItemAsync(SERVER_URL_KEY);
  return saved ?? DEFAULT_API_URL;
}

export const api = axios.create({
  baseURL: DEFAULT_API_URL,
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

// Atualiza a baseURL dinamicamente antes de cada request
api.interceptors.request.use(async (config) => {
  config.baseURL = await getApiUrl();

  const token = await SecureStore.getItemAsync("access_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Trata 401 — limpa token e força re-login
api.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401) {
      await SecureStore.deleteItemAsync("access_token");
      await SecureStore.deleteItemAsync("refresh_token");
    }
    return Promise.reject(error);
  }
);
