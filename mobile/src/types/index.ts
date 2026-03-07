// =============================================================================
// DTOs — espelho do backend (mesmo contrato do frontend web)
// =============================================================================

export type Role = "ADMIN" | "ATENDENTE" | "CLIENTE" | "SUPER_ADMIN";
export type StatusLocacao = "RESERVADO" | "RETIRADO" | "DEVOLVIDO" | "CANCELADO";
export type StatusCaucao = "PENDENTE" | "DEVOLVIDA" | "RETIDA";
export type MetodoPagamento = "DINHEIRO" | "CARTAO_CREDITO" | "CARTAO_DEBITO" | "PIX" | "TRANSFERENCIA";
export type StatusPagamento = "PENDENTE" | "PAGO" | "ESTORNADO";
export type CategoriaMovimentacao = "SAIDA" | "ENTRADA" | "MANUTENCAO";

// Auth
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken?: string;
  tipo: string;
  expiresIn: number;
  username: string;
  roles: string[];
  tenantId?: string;
}

// Produto
export interface ProdutoResponse {
  id: number;
  nome: string;
  categoria: string;
  valorDiaria: number;
  codigo: string;
  disponivel: boolean;
  tamanho?: string;
  cor?: string;
  descricao?: string;
  imagemUrl?: string;
  createdAt: string;
}

export interface ProdutoRequest {
  nome: string;
  categoria: string;
  valorDiaria: number;
  codigo: string;
  tamanho?: string;
  cor?: string;
  descricao?: string;
}

// Cliente
export interface ClienteResponse {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  documento: string;
  ativo: boolean;
  createdAt: string;
}

export interface ClienteRequest {
  nome: string;
  email: string;
  telefone: string;
  documento: string;
}

// Locação
export interface ItemLocacaoResponse {
  id: number;
  produtoId: number;
  nomeProduto: string;
  valorDiaria: number;
  subtotal: number;
}

export interface LocacaoResponse {
  id: number;
  clienteId: number;
  clienteNome?: string;
  itens: ItemLocacaoResponse[];
  dataInicio: string;
  dataFim: string;
  diasLocacao: number;
  status: StatusLocacao;
  caucao: number | null;
  statusCaucao: StatusCaucao;
  caucaoRetida: number | null;
  multa: number | null;
  multaCancelamento: number | null;
  valorTotal: number;
  retiradaAt: string | null;
  devolucaoAt: string | null;
  atrasada: boolean;
  observacoes: string | null;
  createdAt: string;
}

export interface CriarReservaRequest {
  clienteId: number;
  produtoIds: number[];
  dataInicio: string;
  dataFim: string;
  caucao?: number;
  observacoes?: string;
}

// Pagamento
export interface PagamentoResponse {
  id: number;
  locacaoId: number;
  valor: number;
  metodo: MetodoPagamento;
  status: StatusPagamento;
  observacoes?: string;
  criadoEm: string;
}

export interface ProcessarPagamentoRequest {
  locacaoId: number;
  valor: number;
  metodo: MetodoPagamento;
  observacoes?: string;
}

// Estoque
export interface MovimentacaoResponse {
  id: number;
  produtoId: number;
  nomeProduto: string;
  tipo: CategoriaMovimentacao;
  quantidade: number;
  motivo?: string;
  criadoEm: string;
}

// Relatórios
export interface FaturamentoMensalResponse {
  mes: string;
  totalLocacoes: number;
  receitaBruta: number;
  receitaLiquida: number;
}

export interface PecaMaisAlugadaResponse {
  produtoId: number;
  nomeProduto: string;
  totalLocacoes: number;
  receitaTotal: number;
}

export interface AtrasoResponse {
  locacaoId: number;
  clienteNome: string;
  dataFim: string;
  diasAtraso: number;
  multaEstimada: number;
}

// Usuário
export interface UsuarioResponse {
  id: number;
  nome: string;
  username: string;
  email: string;
  role: Role;
  ativo: boolean;
  clienteId: number | null;
  createdAt: string;
}

// Minha Conta
export interface PerfilResponse {
  id: number;
  nome: string;
  username: string;
  email: string;
  role: Role;
  clienteId: number | null;
  createdAt: string;
}

// Paginação
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
