// ============================================================
// Types — espelho dos DTOs do backend (closet-rental API)
// ============================================================

// --- Auth ---
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  roles: string[];
}

// --- Paginação ---
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// --- Catálogo ---
export type CategoriaProduto = "VESTIDO" | "TERNO" | "ACESSORIO" | "CALCADO" | "OUTRO";

export interface ProdutoResponse {
  id: number;
  nome: string;
  categoria: CategoriaProduto;
  valorDiaria: number;
  codigo?: string;
  disponivel: boolean;
  createdBy: string;
}

export interface ProdutoRequest {
  nome: string;
  categoria: CategoriaProduto;
  valorDiaria: number;
  codigo?: string;
  disponivel?: boolean;
}

// --- Clientes ---
export interface ClienteResponse {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  documento: string;
  ativo: boolean;
}

export interface ClienteRequest {
  nome: string;
  email: string;
  telefone: string;
  documento: string;
}

// --- Locações ---
export type StatusLocacao = "RESERVADO" | "RETIRADO" | "DEVOLVIDO" | "CANCELADO";

export interface LocacaoResponse {
  id: number;
  clienteId: number;
  clienteNome?: string;
  produtoId: number;
  produtoNome?: string;
  dataInicio: string;
  dataFim: string;
  valorDiaria: number;
  caucao: number;
  valorTotal: number;
  multaAtraso: number | null;
  multa?: number | null;
  status: StatusLocacao;
  operador?: string;
}

export interface CriarReservaRequest {
  clienteId: number;
  produtoId: number;
  dataInicio: string;
  dataFim: string;
  caucao?: number;
  observacoes?: string;
}

// --- Pagamentos ---
export type StatusPagamento = "PENDENTE" | "APROVADO" | "FALHA" | "ESTORNADO";
export type MetodoPagamento = "PIX" | "CARTAO_CREDITO" | "CARTAO_DEBITO" | "DINHEIRO";

export interface PagamentoResponse {
  id: number;
  locacaoId: number;
  valor: number;
  status: StatusPagamento;
  metodo: MetodoPagamento;
  motivo: string | null;
  criadoEm: string;
  processadoEm: string | null;
}

export interface PagamentoRequest {
  locacaoId: number;
  valor: number;
  metodo: MetodoPagamento;
}

// --- Estoque ---
export type TipoMovimentacao = "SAIDA" | "ENTRADA" | "MANUTENCAO" | "RETORNO_MANUTENCAO";

export interface MovimentacaoEstoqueResponse {
  id: number;
  produtoId: number;
  tipo: TipoMovimentacao;
  motivo: string;
  locacaoId: number | null;
  operador: string;
  criadoEm: string;
}

// --- Relatórios ---
export interface FaturamentoResponse {
  mes: number;
  ano: number;
  totalReceita: number;
  totalPagamentos: number;
}

export interface PecaMaisAlugadaResponse {
  produtoId: number;
  nomeProduto: string;
  totalLocacoes: number;
}

export interface AtrasosPendentesResponse {
  locacaoId: number;
  clienteId: number;
  produtoId: number;
  dataFim: string;
  diasAtraso: number;
  valorDiaria: number;
  multaEstimada: number;
}

// --- Erros da API ---
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
