import { api } from "../api";
import type { ClienteRequest, ClienteResponse, Page } from "@/types";

export const clientesService = {
  listar: async (page = 0, nome?: string): Promise<Page<ClienteResponse>> => {
    const params: Record<string, unknown> = { page, size: 20 };
    if (nome) params.nome = nome;
    const res = await api.get<Page<ClienteResponse>>("/v1/clientes", { params });
    return res.data;
  },

  buscarPorId: async (id: number): Promise<ClienteResponse> => {
    const res = await api.get<ClienteResponse>(`/v1/clientes/${id}`);
    return res.data;
  },

  criar: async (data: ClienteRequest): Promise<ClienteResponse> => {
    const res = await api.post<ClienteResponse>("/v1/clientes", data);
    return res.data;
  },

  atualizar: async (id: number, data: ClienteRequest): Promise<ClienteResponse> => {
    const res = await api.put<ClienteResponse>(`/v1/clientes/${id}`, data);
    return res.data;
  },

  inativar: async (id: number): Promise<void> => {
    await api.patch(`/v1/clientes/${id}/inativar`);
  },

  reativar: async (id: number): Promise<void> => {
    await api.patch(`/v1/clientes/${id}/reativar`);
  },
};
