import { api } from "@/lib/api";
import type { Page, PeriodoOcupadoResponse, ProdutoRequest, ProdutoResponse } from "@/types";

export const catalogoService = {
  listar: (page = 0, size = 12) =>
    api
      .get<Page<ProdutoResponse>>("/v1/catalogo", { params: { page, size } })
      .then((r) => r.data),

  buscarPorId: (id: number) =>
    api.get<ProdutoResponse>(`/v1/catalogo/${id}`).then((r) => r.data),

  buscarDisponiveisNoPeriodo: (dataInicio: string, dataFim: string, page = 0, size = 12) =>
    api
      .get<Page<ProdutoResponse>>("/v1/catalogo/disponiveis", {
        params: { dataInicio, dataFim, page, size },
      })
      .then((r) => r.data),

  criar: (data: ProdutoRequest) =>
    api.post<ProdutoResponse>("/v1/catalogo", data).then((r) => r.data),

  atualizar: (id: number, data: ProdutoRequest) =>
    api.put<ProdutoResponse>(`/v1/catalogo/${id}`, data).then((r) => r.data),

  deletar: (id: number) => api.delete(`/v1/catalogo/${id}`),

  uploadImagem: (id: number, arquivo: File) => {
    const form = new FormData();
    form.append("arquivo", arquivo);
    return api
      .post<ProdutoResponse>(`/v1/catalogo/${id}/imagem`, form, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then((r) => r.data);
  },

  removerImagem: (id: number) =>
    api.delete<ProdutoResponse>(`/v1/catalogo/${id}/imagem`).then((r) => r.data),

  buscarAgenda: (id: number, mes: string) =>
    api
      .get<PeriodoOcupadoResponse[]>(`/v1/catalogo/${id}/agenda`, { params: { mes } })
      .then((r) => r.data),
};
