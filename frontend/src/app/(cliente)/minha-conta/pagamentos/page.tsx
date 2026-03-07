"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ChevronLeft, ChevronRight } from "lucide-react";
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
import { PagamentoBadge } from "@/components/shared/status-badge";
import { minhaContaService } from "@/lib/services/minhaConta.service";
import { formatCurrency, formatDateTime, metodoPagamentoLabel } from "@/lib/format";

export default function MeusPagamentosPage() {
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ["meus-pagamentos", page],
    queryFn: () => minhaContaService.getMeusPagamentos(page),
    staleTime: 30_000,
  });

  const pagamentos = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const totalAprovado = pagamentos
    .filter((p) => p.status === "APROVADO")
    .reduce((s, p) => s + p.valor, 0);

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Meus Pagamentos"
        subtitle={totalElements > 0 ? `${totalElements} registros` : "Histórico de pagamentos"}
      />
      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {/* Card resumo */}
        <div className="rounded-lg border bg-card p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-muted-foreground">Total pago (esta página)</p>
            <p className="text-2xl font-bold">{formatCurrency(totalAprovado)}</p>
          </div>
          <div className="text-right">
            <p className="text-xs text-muted-foreground">Registros nesta página</p>
            <p className="text-2xl font-bold">{pagamentos.length}</p>
          </div>
        </div>

        <div className="rounded-lg border bg-card overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">#</TableHead>
                <TableHead>Locação</TableHead>
                <TableHead>Método</TableHead>
                <TableHead>Valor</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Data</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading &&
                Array.from({ length: 5 }).map((_, i) => (
                  <TableRow key={i}>
                    <TableCell colSpan={6}>
                      <Skeleton className="h-4 w-full" />
                    </TableCell>
                  </TableRow>
                ))}

              {!isLoading && pagamentos.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="py-12 text-center text-muted-foreground">
                    Nenhum pagamento encontrado.
                  </TableCell>
                </TableRow>
              )}

              {pagamentos.map((pag) => (
                <TableRow key={pag.id}>
                  <TableCell className="font-mono text-xs text-muted-foreground">
                    #{pag.id}
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    Locação #{pag.locacaoId}
                  </TableCell>
                  <TableCell className="text-sm">
                    {metodoPagamentoLabel[pag.metodo] ?? pag.metodo}
                  </TableCell>
                  <TableCell className="font-medium">
                    {formatCurrency(pag.valor)}
                  </TableCell>
                  <TableCell>
                    <PagamentoBadge status={pag.status} />
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {formatDateTime(pag.criadoEm)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        {/* Paginação */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between">
            <p className="text-sm text-muted-foreground">
              Página {page + 1} de {totalPages}
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p - 1)}
                disabled={page === 0}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= totalPages - 1}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
