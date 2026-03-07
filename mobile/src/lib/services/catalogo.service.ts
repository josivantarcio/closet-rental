import { api } from "../api";
import type { Page, ProdutoRequest, ProdutoResponse } from "@/types";

export const catalogoService = {
  listar: async (page = 0, size = 20, nome?: string): Promise<Page<ProdutoResponse>> => {
    const params: Record<string, unknown> = { page, size };
    if (nome) params.nome = nome;
    const res = await api.get<Page<ProdutoResponse>>("/v1/catalogo", { params });
    return res.data;
  },

  buscarDisponiveis: async (dataInicio: string, dataFim: string, page = 0): Promise<Page<ProdutoResponse>> => {
    const res = await api.get<Page<ProdutoResponse>>("/v1/catalogo/disponiveis", {
      params: { dataInicio, dataFim, page, size: 20 },
    });
    return res.data;
  },

  buscarPorId: async (id: number): Promise<ProdutoResponse> => {
    const res = await api.get<ProdutoResponse>(`/v1/catalogo/${id}`);
    return res.data;
  },

  criar: async (data: ProdutoRequest): Promise<ProdutoResponse> => {
    const res = await api.post<ProdutoResponse>("/v1/catalogo", data);
    return res.data;
  },

  atualizar: async (id: number, data: ProdutoRequest): Promise<ProdutoResponse> => {
    const res = await api.put<ProdutoResponse>(`/v1/catalogo/${id}`, data);
    return res.data;
  },

  deletar: async (id: number): Promise<void> => {
    await api.delete(`/v1/catalogo/${id}`);
  },
};
