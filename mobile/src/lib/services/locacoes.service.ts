import { api } from "../api";
import type { CriarReservaRequest, LocacaoResponse, Page, StatusLocacao } from "@/types";

export const locacoesService = {
  listar: async (page = 0, status?: StatusLocacao): Promise<Page<LocacaoResponse>> => {
    const params: Record<string, unknown> = { page, size: 20 };
    if (status) params.status = status;
    const res = await api.get<Page<LocacaoResponse>>("/v1/locacoes", { params });
    return res.data;
  },

  buscarPorId: async (id: number): Promise<LocacaoResponse> => {
    const res = await api.get<LocacaoResponse>(`/v1/locacoes/${id}`);
    return res.data;
  },

  criar: async (data: CriarReservaRequest): Promise<LocacaoResponse> => {
    const res = await api.post<LocacaoResponse>("/v1/locacoes", data);
    return res.data;
  },

  registrarRetirada: async (id: number): Promise<LocacaoResponse> => {
    const res = await api.patch<LocacaoResponse>(`/v1/locacoes/${id}/retirar`);
    return res.data;
  },

  registrarDevolucao: async (id: number): Promise<LocacaoResponse> => {
    const res = await api.patch<LocacaoResponse>(`/v1/locacoes/${id}/devolver`);
    return res.data;
  },

  cancelar: async (id: number): Promise<LocacaoResponse> => {
    const res = await api.patch<LocacaoResponse>(`/v1/locacoes/${id}/cancelar`);
    return res.data;
  },
};
