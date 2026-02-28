import { api } from "@/lib/api";
import type {
  AtrasosPendentesResponse,
  FaturamentoResponse,
  Page,
  PecaMaisAlugadaResponse,
} from "@/types";

export const relatoriosService = {
  faturamento: (mes: number, ano: number) =>
    api
      .get<FaturamentoResponse>("/v1/relatorios/faturamento", { params: { mes, ano } })
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
};
