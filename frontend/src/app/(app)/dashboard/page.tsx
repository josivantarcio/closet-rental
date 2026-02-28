"use client";

import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import {
  CalendarDays,
  AlertTriangle,
  TrendingUp,
  Package,
  ArrowRight,
} from "lucide-react";
import Link from "next/link";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { locacoesService } from "@/lib/services/locacoes.service";
import { relatoriosService } from "@/lib/services/relatorios.service";
import { catalogoService } from "@/lib/services/catalogo.service";
import { formatCurrency } from "@/lib/format";
import { LocacaoBadge } from "@/components/shared/status-badge";

function KpiCard({
  title,
  value,
  icon: Icon,
  description,
  loading,
  accent,
}: {
  title: string;
  value: string | number;
  icon: React.ElementType;
  description?: string;
  loading?: boolean;
  accent?: "rose" | "gold" | "default";
}) {
  const accentClass =
    accent === "rose"
      ? "text-rose bg-primary/10"
      : accent === "gold"
      ? "text-gold bg-accent/10"
      : "text-muted-foreground bg-muted";

  return (
    <Card>
      <CardContent className="flex items-start gap-4 pt-6">
        <div className={`rounded-lg p-2.5 ${accentClass}`}>
          <Icon className="h-5 w-5" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm text-muted-foreground">{title}</p>
          {loading ? (
            <Skeleton className="mt-1 h-8 w-24" />
          ) : (
            <p className="mt-1 text-2xl font-bold tracking-tight">{value}</p>
          )}
          {description && <p className="mt-0.5 text-xs text-muted-foreground">{description}</p>}
        </div>
      </CardContent>
    </Card>
  );
}

export default function DashboardPage() {
  const hoje = new Date();
  const mes = hoje.getMonth() + 1;
  const ano = hoje.getFullYear();

  const { data: locacoesAtivas, isLoading: loadingLocacoes } = useQuery({
    queryKey: ["locacoes", "RETIRADO"],
    queryFn: () => locacoesService.listar(0, 5, "RETIRADO"),
  });

  const { data: faturamento, isLoading: loadingFaturamento } = useQuery({
    queryKey: ["faturamento", mes, ano],
    queryFn: () => relatoriosService.faturamento(mes, ano),
  });

  const { data: atrasos, isLoading: loadingAtrasos } = useQuery({
    queryKey: ["atrasos"],
    queryFn: () => relatoriosService.atrasosPendentes(0, 5),
  });

  const { data: catalogo, isLoading: loadingCatalogo } = useQuery({
    queryKey: ["catalogo", 0],
    queryFn: () => catalogoService.listar(0, 1),
  });

  const containerVariants = {
    hidden: { opacity: 0 },
    show: { opacity: 1, transition: { staggerChildren: 0.08 } },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 16 },
    show: { opacity: 1, y: 0 },
  };

  return (
    <div className="flex flex-col overflow-hidden h-full">
      <Header title="Dashboard" subtitle={`${hoje.toLocaleDateString("pt-BR", { weekday: "long", day: "2-digit", month: "long", year: "numeric" })}`} />

      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {/* KPIs */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="show"
          className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4"
        >
          <motion.div variants={itemVariants}>
            <KpiCard
              title="Peças em Uso"
              value={locacoesAtivas?.totalElements ?? 0}
              icon={CalendarDays}
              description="locações ativas (RETIRADO)"
              loading={loadingLocacoes}
              accent="rose"
            />
          </motion.div>
          <motion.div variants={itemVariants}>
            <KpiCard
              title="Atrasos Pendentes"
              value={atrasos?.totalElements ?? 0}
              icon={AlertTriangle}
              description="locações em atraso"
              loading={loadingAtrasos}
              accent="gold"
            />
          </motion.div>
          <motion.div variants={itemVariants}>
            <KpiCard
              title={`Receita — ${mes}/${ano}`}
              value={
                faturamento ? formatCurrency(faturamento.totalReceita) : "—"
              }
              icon={TrendingUp}
              description={`${faturamento?.totalPagamentos ?? 0} pagamentos`}
              loading={loadingFaturamento}
              accent="rose"
            />
          </motion.div>
          <motion.div variants={itemVariants}>
            <KpiCard
              title="Total de Peças"
              value={catalogo?.totalElements ?? 0}
              icon={Package}
              description="no catálogo"
              loading={loadingCatalogo}
            />
          </motion.div>
        </motion.div>

        <div className="grid gap-6 lg:grid-cols-2">
          {/* Locações ativas */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-4">
              <CardTitle className="font-display text-base">Peças em Uso</CardTitle>
              <Button variant="ghost" size="sm" asChild>
                <Link href="/locacoes?status=RETIRADO" className="gap-1 text-xs">
                  Ver todas <ArrowRight className="h-3 w-3" />
                </Link>
              </Button>
            </CardHeader>
            <CardContent>
              {loadingLocacoes ? (
                <div className="space-y-3">
                  {Array.from({ length: 3 }).map((_, i) => (
                    <Skeleton key={i} className="h-12 w-full" />
                  ))}
                </div>
              ) : locacoesAtivas?.content.length === 0 ? (
                <p className="text-sm text-muted-foreground py-4 text-center">
                  Nenhuma locação ativa no momento.
                </p>
              ) : (
                <div className="space-y-2">
                  {locacoesAtivas?.content.map((loc) => (
                    <Link
                      key={loc.id}
                      href={`/locacoes/${loc.id}`}
                      className="flex items-center justify-between rounded-lg border border-border p-3 hover:bg-muted/50 transition-colors"
                    >
                      <div>
                        <p className="text-sm font-medium">Locação #{loc.id}</p>
                        <p className="text-xs text-muted-foreground">
                          Devolução: {new Date(loc.dataFim).toLocaleDateString("pt-BR")}
                        </p>
                      </div>
                      <LocacaoBadge status={loc.status} />
                    </Link>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Atrasos */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-4">
              <CardTitle className="font-display text-base">Atrasos Pendentes</CardTitle>
              <Button variant="ghost" size="sm" asChild>
                <Link href="/relatorios" className="gap-1 text-xs">
                  Ver relatório <ArrowRight className="h-3 w-3" />
                </Link>
              </Button>
            </CardHeader>
            <CardContent>
              {loadingAtrasos ? (
                <div className="space-y-3">
                  {Array.from({ length: 3 }).map((_, i) => (
                    <Skeleton key={i} className="h-12 w-full" />
                  ))}
                </div>
              ) : atrasos?.content.length === 0 ? (
                <p className="text-sm text-muted-foreground py-4 text-center">
                  Nenhum atraso pendente.
                </p>
              ) : (
                <div className="space-y-2">
                  {atrasos?.content.map((a) => (
                    <div
                      key={a.locacaoId}
                      className="flex items-center justify-between rounded-lg border border-amber-500/20 bg-amber-500/5 p-3"
                    >
                      <div>
                        <p className="text-sm font-medium">Locação #{a.locacaoId}</p>
                        <p className="text-xs text-muted-foreground">
                          {a.diasAtraso} dia{a.diasAtraso !== 1 ? "s" : ""} em atraso
                        </p>
                      </div>
                      <p className="text-sm font-semibold text-amber-400">
                        {formatCurrency(a.multaEstimada)}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
