"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { RotateCcw } from "lucide-react";
import { toast } from "sonner";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
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
  { value: "TODOS", label: "Todos" },
  { value: "APROVADO", label: "Aprovado" },
  { value: "PENDENTE", label: "Pendente" },
  { value: "FALHA", label: "Falha" },
  { value: "ESTORNADO", label: "Estornado" },
];

export default function PagamentosPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<StatusPagamento | "TODOS">("TODOS");
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["pagamentos", page, statusFilter],
    queryFn: () =>
      pagamentosService.listar(page, 20, statusFilter === "TODOS" ? undefined : statusFilter),
  });

  const estornarMutation = useMutation({
    mutationFn: (id: number) => pagamentosService.estornar(id, "Estorno solicitado pelo operador"),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pagamentos"] });
      toast.success("Pagamento estornado.");
    },
    onError: () => toast.error("Não é possível estornar este pagamento."),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Pagamentos" subtitle="Histórico de pagamentos" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Select
            value={statusFilter}
            onValueChange={(v) => { setStatusFilter(v as StatusPagamento | "TODOS"); setPage(0); }}
          >
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {statusOptions.map((o) => (
                <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="rounded-lg border border-border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">#</TableHead>
                <TableHead>Locação</TableHead>
                <TableHead>Valor</TableHead>
                <TableHead>Método</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Data</TableHead>
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
                    Nenhum pagamento registrado.
                  </TableCell>
                </TableRow>
              ) : (
                (data?.content ?? []).map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-mono text-xs text-muted-foreground">#{p.id}</TableCell>
                    <TableCell>Locação #{p.locacaoId}</TableCell>
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
