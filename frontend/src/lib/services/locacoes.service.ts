import { api } from "@/lib/api";
import type { CriarReservaRequest, LocacaoResponse, Page, StatusLocacao } from "@/types";

export const locacoesService = {
  listar: (page = 0, size = 20, status?: StatusLocacao) =>
    api
      .get<Page<LocacaoResponse>>("/v1/locacoes", {
        params: { page, size, ...(status ? { status } : {}) },
      })
      .then((r) => r.data),

  buscarPorId: (id: number) =>
    api.get<LocacaoResponse>(`/v1/locacoes/${id}`).then((r) => r.data),

  listarPorCliente: (clienteId: number, page = 0, size = 20) =>
    api
      .get<Page<LocacaoResponse>>(`/v1/locacoes`, {
        params: { clienteId, page, size },
      })
      .then((r) => r.data),

  criarReserva: (data: CriarReservaRequest) =>
    api.post<LocacaoResponse>("/v1/locacoes/reservar", data).then((r) => r.data),

  registrarRetirada: (id: number) =>
    api.post<LocacaoResponse>(`/v1/locacoes/${id}/retirar`).then((r) => r.data),

  registrarDevolucao: (id: number) =>
    api.post<LocacaoResponse>(`/v1/locacoes/${id}/devolver`).then((r) => r.data),

  cancelar: (id: number) =>
    api.post<LocacaoResponse>(`/v1/locacoes/${id}/cancelar`).then((r) => r.data),

  baixarContrato: async (id: number): Promise<void> => {
    const response = await api.get(`/v1/locacoes/${id}/contrato`, {
      responseType: "blob",
    });
    const url = URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = `contrato-locacao-${id}.pdf`;
    link.click();
    URL.revokeObjectURL(url);
  },
};
