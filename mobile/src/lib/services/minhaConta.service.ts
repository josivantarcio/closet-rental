import { api } from "../api";
import type { LocacaoResponse, Page, PagamentoResponse, PerfilResponse } from "@/types";

export const minhaContaService = {
  getPerfil: async (): Promise<PerfilResponse> => {
    const res = await api.get<PerfilResponse>("/v1/minha-conta/perfil");
    return res.data;
  },

  alterarSenha: async (senhaAtual: string, novaSenha: string): Promise<void> => {
    await api.patch("/v1/minha-conta/senha", { senhaAtual, novaSenha });
  },

  getMinhasLocacoes: async (page = 0): Promise<Page<LocacaoResponse>> => {
    const res = await api.get<Page<LocacaoResponse>>("/v1/minha-conta/locacoes", {
      params: { page, size: 20 },
    });
    return res.data;
  },

  getMeusPagamentos: async (page = 0): Promise<Page<PagamentoResponse>> => {
    const res = await api.get<Page<PagamentoResponse>>("/v1/minha-conta/pagamentos", {
      params: { page, size: 20 },
    });
    return res.data;
  },
};
