"use client";

import { use } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Loader2 } from "lucide-react";
import Link from "next/link";
import { motion } from "framer-motion";
import { toast } from "sonner";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { LocacaoBadge } from "@/components/shared/status-badge";
import { locacoesService } from "@/lib/services/locacoes.service";
import { pagamentosService } from "@/lib/services/pagamentos.service";
import { formatCurrency, formatDate } from "@/lib/format";

const steps = ["RESERVADO", "RETIRADO", "DEVOLVIDO"] as const;

export default function LocacaoDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const locacaoId = Number(id);
  const queryClient = useQueryClient();

  const { data: locacao, isLoading } = useQuery({
    queryKey: ["locacao", locacaoId],
    queryFn: () => locacoesService.buscarPorId(locacaoId),
  });

  const { data: pagamentos } = useQuery({
    queryKey: ["pagamentos-locacao", locacaoId],
    queryFn: () => pagamentosService.listar(0, 10, undefined, locacaoId),
    enabled: !!locacaoId,
  });

  const retirarMutation = useMutation({
    mutationFn: () => locacoesService.registrarRetirada(locacaoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locacao", locacaoId] });
      toast.success("Retirada registrada!");
    },
  });

  const devolverMutation = useMutation({
    mutationFn: () => locacoesService.registrarDevolucao(locacaoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locacao", locacaoId] });
      toast.success("Devolução registrada!");
    },
  });

  const cancelarMutation = useMutation({
    mutationFn: () => locacoesService.cancelar(locacaoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locacao", locacaoId] });
      toast.success("Locação cancelada.");
    },
    onError: () => toast.error("Não é possível cancelar."),
  });

  const currentStep = locacao ? steps.indexOf(locacao.status as typeof steps[number]) : -1;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title={`Locação #${locacaoId}`} />

      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        <Button variant="ghost" size="sm" asChild className="gap-2 -ml-2">
          <Link href="/locacoes">
            <ArrowLeft className="h-4 w-4" />
            Voltar
          </Link>
        </Button>

        {isLoading ? (
          <div className="grid gap-4 lg:grid-cols-2">
            <Skeleton className="h-56" />
            <Skeleton className="h-56" />
          </div>
        ) : locacao ? (
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            className="grid gap-6 lg:grid-cols-2"
          >
            {/* Detalhes */}
            <Card>
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <CardTitle className="font-display text-base">Detalhes</CardTitle>
                  <LocacaoBadge status={locacao.status} />
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                  <dt className="text-muted-foreground">Cliente</dt>
                  <dd className="font-medium">#{locacao.clienteId}</dd>
                  <dt className="text-muted-foreground">Produto</dt>
                  <dd className="font-medium">#{locacao.produtoId}</dd>
                  <dt className="text-muted-foreground">Período</dt>
                  <dd>{formatDate(locacao.dataInicio)} → {formatDate(locacao.dataFim)}</dd>
                  <dt className="text-muted-foreground">Valor Diária</dt>
                  <dd>{formatCurrency(locacao.valorDiaria)}</dd>
                  <dt className="text-muted-foreground">Caução</dt>
                  <dd>{formatCurrency(locacao.caucao)}</dd>
                  <dt className="text-muted-foreground">Total</dt>
                  <dd className="font-bold text-primary">{formatCurrency(locacao.valorTotal)}</dd>
                  {locacao.multaAtraso != null && (
                    <>
                      <dt className="text-muted-foreground">Multa</dt>
                      <dd className="text-amber-400 font-semibold">{formatCurrency(locacao.multaAtraso)}</dd>
                    </>
                  )}
                  <dt className="text-muted-foreground">Operador</dt>
                  <dd>{locacao.operador}</dd>
                </dl>

                {/* Ações */}
                {locacao.status !== "CANCELADO" && locacao.status !== "DEVOLVIDO" && (
                  <div className="flex gap-2 pt-2">
                    {locacao.status === "RESERVADO" && (
                      <>
                        <Button
                          size="sm"
                          onClick={() => retirarMutation.mutate()}
                          disabled={retirarMutation.isPending}
                        >
                          {retirarMutation.isPending && <Loader2 className="mr-1.5 h-3 w-3 animate-spin" />}
                          Registrar Retirada
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => cancelarMutation.mutate()}
                          disabled={cancelarMutation.isPending}
                        >
                          Cancelar
                        </Button>
                      </>
                    )}
                    {locacao.status === "RETIRADO" && (
                      <Button
                        size="sm"
                        onClick={() => devolverMutation.mutate()}
                        disabled={devolverMutation.isPending}
                      >
                        {devolverMutation.isPending && <Loader2 className="mr-1.5 h-3 w-3 animate-spin" />}
                        Registrar Devolução
                      </Button>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Timeline de status */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="font-display text-base">Fluxo de Status</CardTitle>
              </CardHeader>
              <CardContent>
                {locacao.status === "CANCELADO" ? (
                  <div className="flex items-center gap-3 rounded-lg bg-zinc-500/10 border border-zinc-500/20 p-4">
                    <span className="text-2xl">✕</span>
                    <div>
                      <p className="font-medium">Locação Cancelada</p>
                      <p className="text-xs text-muted-foreground">Esta locação foi cancelada.</p>
                    </div>
                  </div>
                ) : (
                  <ol className="relative border-l border-border ml-3 space-y-6">
                    {steps.map((step, i) => {
                      const done = currentStep >= i;
                      const active = currentStep === i;
                      return (
                        <li key={step} className="ml-6">
                          <span
                            className={`absolute -left-3 flex h-6 w-6 items-center justify-center rounded-full border-2 text-xs font-bold transition-colors ${
                              done
                                ? "border-primary bg-primary text-primary-foreground"
                                : "border-border bg-background text-muted-foreground"
                            }`}
                          >
                            {done ? "✓" : i + 1}
                          </span>
                          <div className={active ? "text-foreground" : done ? "text-muted-foreground" : "text-muted-foreground/50"}>
                            <p className={`text-sm font-medium ${active ? "text-primary" : ""}`}>
                              {step === "RESERVADO" ? "Reservado" : step === "RETIRADO" ? "Retirado" : "Devolvido"}
                            </p>
                            <p className="text-xs">
                              {step === "RESERVADO" && "Reserva confirmada"}
                              {step === "RETIRADO" && "Peça retirada pelo cliente"}
                              {step === "DEVOLVIDO" && "Peça devolvida ao atelier"}
                            </p>
                          </div>
                        </li>
                      );
                    })}
                  </ol>
                )}

                {/* Pagamentos desta locação */}
                {(pagamentos?.content ?? []).length > 0 && (
                  <div className="mt-6 pt-4 border-t border-border">
                    <p className="text-xs font-medium text-muted-foreground mb-2">Pagamentos</p>
                    <div className="space-y-1.5">
                      {(pagamentos?.content ?? []).map((p) => (
                        <div key={p.id} className="flex items-center justify-between text-sm">
                          <span className="text-muted-foreground">#{p.id} · {p.metodo}</span>
                          <span className="font-medium">{formatCurrency(p.valor)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </motion.div>
        ) : (
          <p className="text-muted-foreground">Locação não encontrada.</p>
        )}
      </div>
    </div>
  );
}
