import { Badge } from "@/components/ui/badge";
import type { StatusLocacao, StatusPagamento } from "@/types";

const locacaoConfig: Record<StatusLocacao, { label: string; className: string }> = {
  RESERVADO: {
    label: "Reservado",
    className: "bg-blue-500/10 text-blue-400 border-blue-500/20",
  },
  RETIRADO: {
    label: "Retirado",
    className: "bg-amber-500/10 text-amber-400 border-amber-500/20",
  },
  DEVOLVIDO: {
    label: "Devolvido",
    className: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20",
  },
  CANCELADO: {
    label: "Cancelado",
    className: "bg-zinc-500/10 text-zinc-400 border-zinc-500/20",
  },
};

const pagamentoConfig: Record<StatusPagamento, { label: string; className: string }> = {
  PENDENTE: {
    label: "Pendente",
    className: "bg-amber-500/10 text-amber-400 border-amber-500/20",
  },
  APROVADO: {
    label: "Aprovado",
    className: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20",
  },
  FALHA: {
    label: "Falha",
    className: "bg-red-500/10 text-red-400 border-red-500/20",
  },
  ESTORNADO: {
    label: "Estornado",
    className: "bg-purple-500/10 text-purple-400 border-purple-500/20",
  },
};

export function LocacaoBadge({ status }: { status: StatusLocacao }) {
  const cfg = locacaoConfig[status];
  return (
    <Badge variant="outline" className={cfg.className}>
      {cfg.label}
    </Badge>
  );
}

export function PagamentoBadge({ status }: { status: StatusPagamento }) {
  const cfg = pagamentoConfig[status];
  return (
    <Badge variant="outline" className={cfg.className}>
      {cfg.label}
    </Badge>
  );
}

export function DisponibilidadeBadge({ disponivel }: { disponivel: boolean }) {
  return disponivel ? (
    <Badge variant="outline" className="bg-emerald-500/10 text-emerald-400 border-emerald-500/20">
      Disponível
    </Badge>
  ) : (
    <Badge variant="outline" className="bg-zinc-500/10 text-zinc-400 border-zinc-500/20">
      Indisponível
    </Badge>
  );
}
