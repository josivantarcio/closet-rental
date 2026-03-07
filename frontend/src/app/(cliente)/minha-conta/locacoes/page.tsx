"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { FileText, ChevronLeft, ChevronRight } from "lucide-react";
import Link from "next/link";
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
import { LocacaoBadge } from "@/components/shared/status-badge";
import { minhaContaService } from "@/lib/services/minhaConta.service";
import { formatCurrency, formatDate } from "@/lib/format";

export default function MinhasLocacoesPage() {
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ["minhas-locacoes", page],
    queryFn: () => minhaContaService.getMinhasLocacoes(page),
    staleTime: 30_000,
  });

  const locacoes = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Minhas Locações"
        subtitle={totalElements > 0 ? `${totalElements} locações encontradas` : "Histórico de locações"}
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="rounded-lg border bg-card overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">#</TableHead>
                <TableHead>Período</TableHead>
                <TableHead>Peças</TableHead>
                <TableHead>Valor Total</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Ações</TableHead>
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

              {!isLoading && locacoes.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="py-12 text-center text-muted-foreground">
                    Nenhuma locação encontrada.
                  </TableCell>
                </TableRow>
              )}

              {locacoes.map((loc) => (
                <TableRow key={loc.id}>
                  <TableCell className="font-mono text-xs text-muted-foreground">
                    #{loc.id}
                  </TableCell>
                  <TableCell className="text-sm">
                    {formatDate(loc.dataInicio)} → {formatDate(loc.dataFim)}
                  </TableCell>
                  <TableCell className="text-sm">
                    {loc.itens?.length ?? 0} peça(s)
                  </TableCell>
                  <TableCell className="font-medium">
                    {formatCurrency(loc.valorTotal)}
                  </TableCell>
                  <TableCell>
                    <LocacaoBadge status={loc.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <Button asChild variant="ghost" size="sm">
                        <Link href={`/minha-conta/locacoes/${loc.id}/contrato`}>
                          <FileText className="h-4 w-4 mr-1" />
                          Contrato
                        </Link>
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        {/* Paginação */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between mt-4">
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
