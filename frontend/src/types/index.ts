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
  tipo: string;
  expiresIn: number;
  username: string;
  roles: string[];
  refreshToken?: string;
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
  tamanho?: string;
  cor?: string;
  descricao?: string;
  imagemUrl?: string;
  createdBy: string;
}

export interface ProdutoRequest {
  nome: string;
  categoria: CategoriaProduto;
  valorDiaria: number;
  codigo?: string;
  disponivel?: boolean;
  tamanho?: string;
  cor?: string;
  descricao?: string;
  imagemUrl?: string;
}

// --- Clientes ---
export interface ClienteResponse {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  documento: string;           // disponível apenas em buscarPorId (ADMIN)
  documentoMascarado?: string; // disponível na listagem (LGPD)
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

export interface FaturamentoPeriodoResponse {
  meses: FaturamentoResponse[];
  totalPeriodo: number;
  totalPagamentosPeriodo: number;
}

export interface PecaMaisAlugadaResponse {
  produtoId: number;
  nomeProduto: string;
  nome: string;
  categoria: string;
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

export interface TopClienteResponse {
  clienteId: number;
  nomeCliente: string;
  totalLocacoes: number;
  receitaTotal: number;
}

export interface TicketMedioResponse {
  mes: number;
  ano: number;
  totalLocacoes: number;
  receitaTotal: number;
  ticketMedio: number;
}

export interface ReceitaCategoriaResponse {
  categoria: string;
  totalLocacoes: number;
  receitaTotal: number;
}

// --- Erros da API ---
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
