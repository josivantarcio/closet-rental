import { api } from "@/lib/api";
import type { Page, PagamentoRequest, PagamentoResponse, StatusPagamento } from "@/types";

export const pagamentosService = {
  listar: (page = 0, size = 20, status?: StatusPagamento, locacaoId?: number) =>
    api
      .get<Page<PagamentoResponse>>("/v1/pagamentos", {
        params: { page, size, ...(status ? { status } : {}), ...(locacaoId ? { locacaoId } : {}) },
      })
      .then((r) => r.data),

  buscarPorId: (id: number) =>
    api.get<PagamentoResponse>(`/v1/pagamentos/${id}`).then((r) => r.data),

  processar: (data: PagamentoRequest) =>
    api.post<PagamentoResponse>("/v1/pagamentos", data).then((r) => r.data),

  estornar: (id: number, motivo?: string) =>
    api
      .post<PagamentoResponse>(`/v1/pagamentos/${id}/estornar`, { motivo })
      .then((r) => r.data),
};
