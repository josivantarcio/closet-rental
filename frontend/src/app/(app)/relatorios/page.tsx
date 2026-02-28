"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { TrendingUp, AlertTriangle, Trophy } from "lucide-react";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { relatoriosService } from "@/lib/services/relatorios.service";
import { formatCurrency, formatDate } from "@/lib/format";

export default function RelatoriosPage() {
  const hoje = new Date();
  const [mes, setMes] = useState(hoje.getMonth() + 1);
  const [ano, setAno] = useState(hoje.getFullYear());
  const [mesBusca, setMesBusca] = useState(hoje.getMonth() + 1);
  const [anoBusca, setAnoBusca] = useState(hoje.getFullYear());

  const { data: faturamento, isLoading: loadingFat } = useQuery({
    queryKey: ["faturamento", mes, ano],
    queryFn: () => relatoriosService.faturamento(mes, ano),
  });

  const { data: pecas, isLoading: loadingPecas } = useQuery({
    queryKey: ["pecas-mais-alugadas"],
    queryFn: () => relatoriosService.pecasMaisAlugadas(10),
  });

  const { data: atrasos, isLoading: loadingAtrasos } = useQuery({
    queryKey: ["atrasos-pendentes"],
    queryFn: () => relatoriosService.atrasosPendentes(0, 20),
  });

  const containerVariants = {
    hidden: { opacity: 0 },
    show: { opacity: 1, transition: { staggerChildren: 0.1 } },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 12 },
    show: { opacity: 1, y: 0 },
  };

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Relatórios" subtitle="Análise gerencial do atelier" />

      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="show"
          className="grid gap-6 lg:grid-cols-3"
        >
          {/* Faturamento */}
          <motion.div variants={itemVariants} className="lg:col-span-1">
            <Card className="h-full">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-2">
                  <TrendingUp className="h-4 w-4 text-primary" />
                  <CardTitle className="font-display text-base">Faturamento</CardTitle>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex gap-2">
                  <div className="flex-1 space-y-1">
                    <Label className="text-xs">Mês</Label>
                    <Input
                      type="number"
                      min={1}
                      max={12}
                      value={mesBusca}
                      onChange={(e) => setMesBusca(Number(e.target.value))}
                      className="h-8"
                    />
                  </div>
                  <div className="flex-1 space-y-1">
                    <Label className="text-xs">Ano</Label>
                    <Input
                      type="number"
                      min={2000}
                      max={2100}
                      value={anoBusca}
                      onChange={(e) => setAnoBusca(Number(e.target.value))}
                      className="h-8"
                    />
                  </div>
                  <div className="flex items-end">
                    <Button
                      size="sm"
                      className="h-8"
                      onClick={() => { setMes(mesBusca); setAno(anoBusca); }}
                    >
                      Buscar
                    </Button>
                  </div>
                </div>

                {loadingFat ? (
                  <div className="space-y-2">
                    <Skeleton className="h-10 w-full" />
                    <Skeleton className="h-6 w-32" />
                  </div>
                ) : faturamento ? (
                  <div className="rounded-lg bg-primary/5 border border-primary/10 p-4">
                    <p className="text-xs text-muted-foreground mb-1">
                      {mes.toString().padStart(2, "0")}/{ano}
                    </p>
                    <p className="text-3xl font-bold font-display text-primary">
                      {formatCurrency(faturamento.totalReceita)}
                    </p>
                    <p className="text-xs text-muted-foreground mt-1">
                      {faturamento.totalPagamentos} pagamento{faturamento.totalPagamentos !== 1 ? "s" : ""}
                    </p>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground text-center py-4">
                    Sem dados para este período.
                  </p>
                )}
              </CardContent>
            </Card>
          </motion.div>

          {/* Peças mais alugadas */}
          <motion.div variants={itemVariants} className="lg:col-span-1">
            <Card className="h-full">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-2">
                  <Trophy className="h-4 w-4 text-amber-400" />
                  <CardTitle className="font-display text-base">Peças Mais Alugadas</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                {loadingPecas ? (
                  <div className="space-y-2">
                    {Array.from({ length: 5 }).map((_, i) => (
                      <Skeleton key={i} className="h-10 w-full" />
                    ))}
                  </div>
                ) : (pecas ?? []).length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-4">
                    Nenhum dado disponível.
                  </p>
                ) : (
                  <ol className="space-y-2">
                    {(pecas ?? []).map((p, i) => (
                      <li
                        key={p.produtoId}
                        className="flex items-center gap-3 rounded-lg border border-border p-2.5"
                      >
                        <span className={`text-lg font-bold font-display w-6 text-center ${
                          i === 0 ? "text-amber-400" : i === 1 ? "text-zinc-300" : i === 2 ? "text-amber-600" : "text-muted-foreground"
                        }`}>
                          {i + 1}
                        </span>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium truncate">{p.nomeProduto}</p>
                          <p className="text-xs text-muted-foreground">ID #{p.produtoId}</p>
                        </div>
                        <span className="text-sm font-semibold tabular-nums">
                          {p.totalLocacoes}x
                        </span>
                      </li>
                    ))}
                  </ol>
                )}
              </CardContent>
            </Card>
          </motion.div>

          {/* Atrasos */}
          <motion.div variants={itemVariants} className="lg:col-span-1">
            <Card className="h-full">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-2">
                  <AlertTriangle className="h-4 w-4 text-amber-400" />
                  <CardTitle className="font-display text-base">Atrasos Pendentes</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                {loadingAtrasos ? (
                  <div className="space-y-2">
                    {Array.from({ length: 4 }).map((_, i) => (
                      <Skeleton key={i} className="h-14 w-full" />
                    ))}
                  </div>
                ) : (atrasos?.content ?? []).length === 0 ? (
                  <div className="text-center py-6">
                    <p className="text-2xl mb-1">✓</p>
                    <p className="text-sm text-muted-foreground">Nenhum atraso pendente!</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {(atrasos?.content ?? []).map((a) => (
                      <div
                        key={a.locacaoId}
                        className="rounded-lg border border-amber-500/20 bg-amber-500/5 p-3"
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm font-medium">Locação #{a.locacaoId}</p>
                            <p className="text-xs text-muted-foreground">
                              Venceu em {formatDate(a.dataFim)} · {a.diasAtraso}d atraso
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="text-xs text-muted-foreground">Multa</p>
                            <p className="text-sm font-semibold text-amber-400">
                              {formatCurrency(a.multaEstimada)}
                            </p>
                          </div>
                        </div>
                      </div>
                    ))}
                    {(atrasos?.totalElements ?? 0) > (atrasos?.content.length ?? 0) && (
                      <p className="text-xs text-center text-muted-foreground pt-1">
                        + {(atrasos?.totalElements ?? 0) - (atrasos?.content.length ?? 0)} mais
                      </p>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>
          </motion.div>
        </motion.div>
      </div>
    </div>
  );
}
