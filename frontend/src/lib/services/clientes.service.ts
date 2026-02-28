import { api } from "@/lib/api";
import type { ClienteRequest, ClienteResponse, Page } from "@/types";

export const clientesService = {
  listar: (page = 0, size = 20) =>
    api
      .get<Page<ClienteResponse>>("/v1/clientes", { params: { page, size } })
      .then((r) => r.data),

  buscarPorId: (id: number) =>
    api.get<ClienteResponse>(`/v1/clientes/${id}`).then((r) => r.data),

  criar: (data: ClienteRequest) =>
    api.post<ClienteResponse>("/v1/clientes", data).then((r) => r.data),

  atualizar: (id: number, data: ClienteRequest) =>
    api.put<ClienteResponse>(`/v1/clientes/${id}`, data).then((r) => r.data),

  inativar: (id: number) =>
    api.patch<ClienteResponse>(`/v1/clientes/${id}/inativar`).then((r) => r.data),

  reativar: (id: number) =>
    api.patch<ClienteResponse>(`/v1/clientes/${id}/reativar`).then((r) => r.data),

  deletar: (id: number) => api.delete(`/v1/clientes/${id}`),
};
