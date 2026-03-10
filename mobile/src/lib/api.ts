import axios from "axios";
import * as SecureStore from "expo-secure-store";

export const SERVER_URL_KEY = "server_url";

const DEFAULT_API_URL = process.env.EXPO_PUBLIC_API_URL ?? "http://10.0.2.2:8080";

export async function getApiUrl(): Promise<string> {
  const saved = await SecureStore.getItemAsync(SERVER_URL_KEY);
  return (saved ?? DEFAULT_API_URL).trim().replace(/\/$/, "");
}

export const api = axios.create({
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

// Define a URL base e injeta Bearer token antes de cada request
api.interceptors.request.use(async (config) => {
  const baseURL = await getApiUrl();
  // Constrói a URL completa substituindo baseURL diretamente na url da request
  if (config.url && !config.url.startsWith("http")) {
    config.url = `${baseURL}${config.url}`;
  }

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
