import { api } from "../api";
import type { AtrasoResponse, FaturamentoMensalResponse, PecaMaisAlugadaResponse } from "@/types";

export const relatoriosService = {
  faturamento: async (inicio: string, fim: string): Promise<FaturamentoMensalResponse[]> => {
    const res = await api.get<FaturamentoMensalResponse[]>("/v1/relatorios/faturamento", {
      params: { inicio, fim },
    });
    return res.data;
  },

  pecasMaisAlugadas: async (inicio: string, fim: string, top = 10): Promise<PecaMaisAlugadaResponse[]> => {
    const res = await api.get<PecaMaisAlugadaResponse[]>("/v1/relatorios/pecas-mais-alugadas", {
      params: { inicio, fim, top },
    });
    return res.data;
  },

  atrasos: async (): Promise<AtrasoResponse[]> => {
    const res = await api.get<AtrasoResponse[]>("/v1/relatorios/atrasos");
    return res.data;
  },
};
