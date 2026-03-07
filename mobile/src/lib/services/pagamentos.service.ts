import { api } from "../api";
import type { Page, PagamentoResponse, ProcessarPagamentoRequest } from "@/types";

export const pagamentosService = {
  listar: async (page = 0): Promise<Page<PagamentoResponse>> => {
    const res = await api.get<Page<PagamentoResponse>>("/v1/pagamentos", { params: { page, size: 20 } });
    return res.data;
  },

  processar: async (data: ProcessarPagamentoRequest): Promise<PagamentoResponse> => {
    const res = await api.post<PagamentoResponse>("/v1/pagamentos", data);
    return res.data;
  },

  estornar: async (id: number): Promise<PagamentoResponse> => {
    const res = await api.patch<PagamentoResponse>(`/v1/pagamentos/${id}/estornar`);
    return res.data;
  },
};
