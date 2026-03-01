"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { RotateCcw, CreditCard, CheckCircle2, XCircle, ArrowUpCircle, Link2 } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PagamentoBadge } from "@/components/shared/status-badge";
import { pagamentosService } from "@/lib/services/pagamentos.service";
import { formatCurrency, formatDateTime, metodoPagamentoLabel } from "@/lib/format";
import type { StatusPagamento } from "@/types";

const statusOptions: { value: StatusPagamento | "TODOS"; label: string }[] = [
  { value: "TODOS", label: "Todos os status" },
  { value: "APROVADO", label: "Aprovado" },
  { value: "PENDENTE", label: "Pendente" },
  { value: "FALHA", label: "Falha" },
  { value: "ESTORNADO", label: "Estornado" },
];

export default function PagamentosPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<StatusPagamento | "TODOS">("TODOS");
  const queryClient = useQueryClient();

  // Carrega todos sem filtro para os cards de resumo
  const { data: todos } = useQuery({
    queryKey: ["pagamentos-todos"],
    queryFn: () => pagamentosService.listar(0, 200),
    staleTime: 30_000,
  });

  const { data, isLoading } = useQuery({
    queryKey: ["pagamentos", page, statusFilter],
    queryFn: () =>
      pagamentosService.listar(page, 20, statusFilter === "TODOS" ? undefined : statusFilter),
  });

  const estornarMutation = useMutation({
    mutationFn: (id: number) => pagamentosService.estornar(id, "Estorno solicitado pelo operador"),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pagamentos"] });
      queryClient.invalidateQueries({ queryKey: ["pagamentos-todos"] });
      toast.success("Pagamento estornado.");
    },
    onError: () => toast.error("Não é possível estornar este pagamento."),
  });

  // Cálculos para os cards
  const allPagamentos = todos?.content ?? [];
  const totalAprovados = allPagamentos.filter((p) => p.status === "APROVADO").length;
  const totalEstornados = allPagamentos.filter((p) => p.status === "ESTORNADO").length;
  const totalFalhas = allPagamentos.filter((p) => p.status === "FALHA").length;
  const receitaAprovada = allPagamentos
    .filter((p) => p.status === "APROVADO")
    .reduce((acc, p) => acc + p.valor, 0);

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Pagamentos" subtitle="Histórico de pagamentos" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {/* Cards de resumo */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <CheckCircle2 className="h-8 w-8 text-emerald-400 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Aprovados</p>
                <p className="text-2xl font-bold text-emerald-400">{totalAprovados}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <CreditCard className="h-8 w-8 text-primary shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Receita Total</p>
                <p className="text-lg font-bold text-primary">{formatCurrency(receitaAprovada)}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <ArrowUpCircle className="h-8 w-8 text-purple-400 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Estornados</p>
                <p className="text-2xl font-bold text-purple-400">{totalEstornados}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <XCircle className="h-8 w-8 text-rose-400 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Falhas</p>
                <p className="text-2xl font-bold text-rose-400">{totalFalhas}</p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Filtro */}
        <div className="flex items-center gap-3">
          <Select
            value={statusFilter}
            onValueChange={(v) => { setStatusFilter(v as StatusPagamento | "TODOS"); setPage(0); }}
          >
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {statusOptions.map((o) => (
                <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
          {!isLoading && data && (
            <span className="text-xs text-muted-foreground">
              {data.totalElements} pagamento(s)
            </span>
          )}
        </div>

        {/* Tabela */}
        <div className="rounded-lg border border-border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">#</TableHead>
                <TableHead>Locação</TableHead>
                <TableHead>Valor</TableHead>
                <TableHead>Método</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Processado em</TableHead>
                <TableHead className="w-20">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <TableRow key={i}>
                    {Array.from({ length: 7 }).map((_, j) => (
                      <TableCell key={j}><Skeleton className="h-4 w-full" /></TableCell>
                    ))}
                  </TableRow>
                ))
              ) : (data?.content ?? []).length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground py-10">
                    Nenhum pagamento encontrado.
                  </TableCell>
                </TableRow>
              ) : (
                (data?.content ?? []).map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-mono text-xs text-muted-foreground">#{p.id}</TableCell>
                    <TableCell>
                      <Link
                        href={`/locacoes/${p.locacaoId}`}
                        className="flex items-center gap-1 text-sm hover:text-primary transition-colors"
                      >
                        <Link2 className="h-3 w-3 shrink-0" />
                        Locação #{p.locacaoId}
                      </Link>
                    </TableCell>
                    <TableCell className="font-semibold">{formatCurrency(p.valor)}</TableCell>
                    <TableCell className="text-sm">{metodoPagamentoLabel[p.metodo]}</TableCell>
                    <TableCell><PagamentoBadge status={p.status} /></TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {p.processadoEm ? formatDateTime(p.processadoEm) : "—"}
                    </TableCell>
                    <TableCell>
                      {p.status === "APROVADO" && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-muted-foreground hover:text-purple-400"
                          title="Estornar pagamento"
                          onClick={() => {
                            if (confirm("Estornar este pagamento?")) estornarMutation.mutate(p.id);
                          }}
                        >
                          <RotateCcw className="h-3.5 w-3.5" />
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-center gap-2">
            <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Anterior</Button>
            <span className="text-sm text-muted-foreground">{page + 1} / {data.totalPages}</span>
            <Button variant="outline" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Próxima</Button>
          </div>
        )}
      </div>
    </div>
  );
}
