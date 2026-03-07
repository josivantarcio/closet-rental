import { api } from "@/lib/api";
import type {
  AtrasosPendentesResponse,
  FaturamentoPeriodoResponse,
  FaturamentoResponse,
  Page,
  PecaMaisAlugadaResponse,
  ReceitaCategoriaResponse,
  TicketMedioResponse,
  TopClienteResponse,
} from "@/types";

export const relatoriosService = {
  faturamento: (mes: number, ano: number) =>
    api
      .get<FaturamentoResponse>("/v1/relatorios/faturamento", { params: { mes, ano } })
      .then((r) => r.data),

  faturamentoPorPeriodo: (de: string, ate: string) =>
    api
      .get<FaturamentoPeriodoResponse>("/v1/relatorios/faturamento/periodo", { params: { de, ate } })
      .then((r) => r.data),

  pecasMaisAlugadas: (limite = 10) =>
    api
      .get<PecaMaisAlugadaResponse[]>("/v1/relatorios/pecas-mais-alugadas", { params: { limite } })
      .then((r) => r.data),

  atrasosPendentes: (page = 0, size = 20) =>
    api
      .get<Page<AtrasosPendentesResponse>>("/v1/relatorios/atrasos-pendentes", {
        params: { page, size },
      })
      .then((r) => r.data),

  topClientes: (limite = 10) =>
    api
      .get<TopClienteResponse[]>("/v1/relatorios/top-clientes", { params: { limite } })
      .then((r) => r.data),

  ticketMedio: (de: string, ate: string) =>
    api
      .get<TicketMedioResponse[]>("/v1/relatorios/ticket-medio", { params: { de, ate } })
      .then((r) => r.data),

  receitaPorCategoria: () =>
    api
      .get<ReceitaCategoriaResponse[]>("/v1/relatorios/receita-por-categoria")
      .then((r) => r.data),

  exportar: (tipo: string, formato: "CSV" | "PDF", de?: string, ate?: string) =>
    api
      .get("/v1/relatorios/exportar", {
        params: { tipo, formato, de, ate },
        responseType: "blob",
      })
      .then((r) => {
        const ext = formato === "PDF" ? "pdf" : "csv";
        const url = URL.createObjectURL(new Blob([r.data]));
        const link = document.createElement("a");
        link.href = url;
        link.download = `${tipo}.${ext}`;
        link.click();
        URL.revokeObjectURL(url);
      }),
};
