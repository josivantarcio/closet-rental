import { format, parseISO } from "date-fns";
import { ptBR } from "date-fns/locale";

export const formatCurrency = (value: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);

export const formatDate = (dateStr: string) =>
  format(parseISO(dateStr), "dd/MM/yyyy", { locale: ptBR });

export const formatDateTime = (dateStr: string) =>
  format(parseISO(dateStr), "dd/MM/yyyy HH:mm", { locale: ptBR });

export const formatDocumento = (doc: string | null | undefined) => {
  if (!doc) return "—";
  const digits = doc.replace(/\D/g, "");
  if (digits.length === 11) {
    // CPF: 000.000.000-00
    return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
  }
  // CNPJ: 00.000.000/0000-00
  return digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
};

export const metodoPagamentoLabel: Record<string, string> = {
  PIX: "PIX",
  CARTAO_CREDITO: "Cartão de Crédito",
  CARTAO_DEBITO: "Cartão de Débito",
  DINHEIRO: "Dinheiro",
};

export const categoriaLabel: Record<string, string> = {
  VESTIDO: "Vestido",
  TERNO: "Terno",
  ACESSORIO: "Acessório",
  CALCADO: "Calçado",
  "CALÇADO": "Calçado",
  OUTRO: "Outro",
};
