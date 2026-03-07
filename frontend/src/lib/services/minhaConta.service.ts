import { api } from "@/lib/api";
import type {
  AlterarMinhaSenhaRequest,
  LocacaoResponse,
  Page,
  PagamentoResponse,
  PerfilResponse,
} from "@/types";

export const minhaContaService = {
  async getPerfil(): Promise<PerfilResponse> {
    const { data } = await api.get("/v1/minha-conta/perfil");
    return data;
  },

  async alterarSenha(payload: AlterarMinhaSenhaRequest): Promise<void> {
    await api.patch("/v1/minha-conta/senha", payload);
  },

  async getMinhasLocacoes(page = 0, size = 20): Promise<Page<LocacaoResponse>> {
    const { data } = await api.get("/v1/minha-conta/locacoes", {
      params: { page, size, sort: "id,desc" },
    });
    return data;
  },

  async getMeusPagamentos(page = 0, size = 20): Promise<Page<PagamentoResponse>> {
    const { data } = await api.get("/v1/minha-conta/pagamentos", {
      params: { page, size },
    });
    return data;
  },

  getContratoUrl(locacaoId: number): string {
    return `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/v1/minha-conta/locacoes/${locacaoId}/contrato`;
  },
};
