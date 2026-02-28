"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Header } from "@/components/layout/header";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import { formatDateTime } from "@/lib/format";
import type { MovimentacaoEstoqueResponse, Page } from "@/types";

const tipoConfig: Record<string, { label: string; className: string }> = {
  SAIDA: { label: "Saída", className: "bg-rose-500/10 text-rose-400 border-rose-500/20" },
  ENTRADA: { label: "Entrada", className: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20" },
  MANUTENCAO: { label: "Manutenção", className: "bg-amber-500/10 text-amber-400 border-amber-500/20" },
  RETORNO_MANUTENCAO: { label: "Retorno Manutenção", className: "bg-blue-500/10 text-blue-400 border-blue-500/20" },
};

export default function EstoquePage() {
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ["estoque-historico", page],
    queryFn: () =>
      api
        .get<Page<MovimentacaoEstoqueResponse>>("/v1/estoque/historico", {
          params: { page, size: 20 },
        })
        .then((r) => r.data),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Estoque" subtitle="Histórico de movimentações" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        <div className="rounded-lg border border-border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">#</TableHead>
                <TableHead>Produto</TableHead>
                <TableHead>Tipo</TableHead>
                <TableHead>Motivo</TableHead>
                <TableHead>Locação</TableHead>
                <TableHead>Operador</TableHead>
                <TableHead>Data</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                Array.from({ length: 6 }).map((_, i) => (
                  <TableRow key={i}>
                    {Array.from({ length: 7 }).map((_, j) => (
                      <TableCell key={j}><Skeleton className="h-4 w-full" /></TableCell>
                    ))}
                  </TableRow>
                ))
              ) : (data?.content ?? []).length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground py-10">
                    Nenhuma movimentação registrada.
                  </TableCell>
                </TableRow>
              ) : (
                (data?.content ?? []).map((m) => {
                  const cfg = tipoConfig[m.tipo] ?? { label: m.tipo, className: "" };
                  return (
                    <TableRow key={m.id}>
                      <TableCell className="font-mono text-xs text-muted-foreground">#{m.id}</TableCell>
                      <TableCell>Produto #{m.produtoId}</TableCell>
                      <TableCell>
                        <Badge variant="outline" className={cfg.className}>{cfg.label}</Badge>
                      </TableCell>
                      <TableCell className="text-sm max-w-48 truncate">{m.motivo}</TableCell>
                      <TableCell className="text-muted-foreground">
                        {m.locacaoId ? `#${m.locacaoId}` : "—"}
                      </TableCell>
                      <TableCell className="text-sm">{m.operador}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        {formatDateTime(m.criadoEm)}
                      </TableCell>
                    </TableRow>
                  );
                })
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
