"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import {
  TrendingUp, AlertTriangle, Trophy, Users, PieChartIcon,
  Download, BarChart2, Activity,
} from "lucide-react";
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from "recharts";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { relatoriosService } from "@/lib/services/relatorios.service";
import { formatCurrency, formatDate } from "@/lib/format";

// ── Cores ─────────────────────────────────────────────────────────────────────
const CHART_COLORS = ["#a78bfa", "#60a5fa", "#34d399", "#f59e0b", "#f87171", "#e879f9"];

// ── Helpers ───────────────────────────────────────────────────────────────────
function isoDate(d: Date) {
  return d.toISOString().split("T")[0];
}

function mesLabel(mes: number, ano: number) {
  return new Date(ano, mes - 1).toLocaleString("pt-BR", { month: "short", year: "2-digit" });
}

// ── Seletor de período ────────────────────────────────────────────────────────
const PERIODOS = [
  { label: "3 meses", meses: 3 },
  { label: "6 meses", meses: 6 },
  { label: "12 meses", meses: 12 },
] as const;

function usePeriodo(meses: number) {
  const ate = new Date();
  const de  = new Date();
  de.setMonth(de.getMonth() - meses + 1);
  de.setDate(1);
  return { de: isoDate(de), ate: isoDate(ate) };
}

// ── Página ────────────────────────────────────────────────────────────────────
export default function RelatoriosPage() {
  const [periodo, setPeriodo] = useState<3 | 6 | 12>(6);
  const { de, ate } = usePeriodo(periodo);

  const { data: faturamento, isLoading: loadFat } = useQuery({
    queryKey: ["faturamento-periodo", de, ate],
    queryFn: () => relatoriosService.faturamentoPorPeriodo(de, ate),
  });

  const { data: pecas, isLoading: loadPecas } = useQuery({
    queryKey: ["pecas-mais-alugadas"],
    queryFn: () => relatoriosService.pecasMaisAlugadas(10),
  });

  const { data: categorias, isLoading: loadCat } = useQuery({
    queryKey: ["receita-categoria"],
    queryFn: () => relatoriosService.receitaPorCategoria(),
  });

  const { data: topClientes, isLoading: loadClientes } = useQuery({
    queryKey: ["top-clientes"],
    queryFn: () => relatoriosService.topClientes(10),
  });

  const { data: ticket, isLoading: loadTicket } = useQuery({
    queryKey: ["ticket-medio", de, ate],
    queryFn: () => relatoriosService.ticketMedio(de, ate),
  });

  const { data: atrasos, isLoading: loadAtrasos } = useQuery({
    queryKey: ["atrasos-pendentes"],
    queryFn: () => relatoriosService.atrasosPendentes(0, 20),
  });

  // dados formatados para gráficos
  const fatData = (faturamento?.meses ?? []).map((m) => ({
    name: mesLabel(m.mes, m.ano),
    receita: Number(m.totalReceita),
    pagamentos: m.totalPagamentos,
  }));

  const ticketData = (ticket ?? []).map((t) => ({
    name: mesLabel(t.mes, t.ano),
    ticket: Number(t.ticketMedio),
  }));

  const pecasData = (pecas ?? []).slice(0, 8).map((p) => ({
    name: (p.nome ?? p.nomeProduto ?? "").length > 18
      ? (p.nome ?? p.nomeProduto ?? "").slice(0, 18) + "…"
      : (p.nome ?? p.nomeProduto ?? ""),
    locacoes: p.totalLocacoes,
  }));

  const catData = (categorias ?? []).map((c) => ({
    name: c.categoria,
    value: Number(c.receitaTotal),
  }));

  const totalPeriodo = faturamento?.totalPeriodo ?? 0;
  const totalAtrasos = atrasos?.totalElements ?? 0;
  const topCliente   = topClientes?.[0];

  const fade = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0 } };

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Relatórios" subtitle="Análise gerencial do atelier" />

      <div className="flex-1 overflow-y-auto p-6 space-y-6">

        {/* Seletor de período + exportações */}
        <div className="flex items-center justify-between flex-wrap gap-3">
          <div className="flex gap-2">
            {PERIODOS.map((p) => (
              <Button
                key={p.meses}
                size="sm"
                variant={periodo === p.meses ? "default" : "outline"}
                onClick={() => setPeriodo(p.meses as 3 | 6 | 12)}
              >
                {p.label}
              </Button>
            ))}
          </div>
          <div className="flex gap-2">
            <Button size="sm" variant="outline" className="gap-1.5"
              onClick={() => relatoriosService.exportar("faturamento", "CSV", de, ate)}>
              <Download className="h-3.5 w-3.5" />CSV Faturamento
            </Button>
            <Button size="sm" variant="outline" className="gap-1.5"
              onClick={() => relatoriosService.exportar("faturamento", "PDF", de, ate)}>
              <Download className="h-3.5 w-3.5" />PDF Faturamento
            </Button>
          </div>
        </div>

        {/* KPIs rápidos */}
        <motion.div
          variants={{ hidden: { opacity: 0 }, show: { opacity: 1, transition: { staggerChildren: 0.08 } } }}
          initial="hidden" animate="show"
          className="grid gap-4 grid-cols-2 lg:grid-cols-4"
        >
          {[
            {
              label: "Receita no período",
              value: loadFat ? null : formatCurrency(totalPeriodo),
              icon: <TrendingUp className="h-4 w-4 text-primary" />,
            },
            {
              label: "Locações em atraso",
              value: loadAtrasos ? null : String(totalAtrasos),
              icon: <AlertTriangle className="h-4 w-4 text-amber-400" />,
            },
            {
              label: "Top cliente",
              value: loadClientes ? null : (topCliente?.nomeCliente ?? "—"),
              icon: <Users className="h-4 w-4 text-emerald-400" />,
            },
            {
              label: "Exportar top clientes",
              value: null,
              icon: <Download className="h-4 w-4 text-muted-foreground" />,
              action: () => relatoriosService.exportar("top-clientes", "CSV"),
            },
          ].map((kpi, i) => (
            <motion.div key={i} variants={fade}>
              <Card
                className={kpi.action ? "cursor-pointer hover:bg-muted/50 transition-colors" : ""}
                onClick={kpi.action}
              >
                <CardContent className="p-4">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-xs text-muted-foreground">{kpi.label}</p>
                    {kpi.icon}
                  </div>
                  {kpi.value === null && !kpi.action ? (
                    <Skeleton className="h-7 w-28" />
                  ) : (
                    <p className="text-xl font-bold font-display truncate">
                      {kpi.value ?? "CSV"}
                    </p>
                  )}
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>

        {/* Linha 1: Faturamento (linha) + Ticket médio (linha) */}
        <div className="grid gap-6 lg:grid-cols-2">
          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center gap-2">
                  <TrendingUp className="h-4 w-4 text-primary" />
                  <CardTitle className="text-sm font-display">Faturamento Mensal</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                {loadFat ? <Skeleton className="h-48" /> : fatData.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-12">Sem dados no período.</p>
                ) : (
                  <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={fatData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#27272a" />
                      <XAxis dataKey="name" tick={{ fontSize: 11, fill: "#71717a" }} />
                      <YAxis tick={{ fontSize: 11, fill: "#71717a" }}
                        tickFormatter={(v) => `R$${(v / 1000).toFixed(0)}k`} />
                      <Tooltip
                        contentStyle={{ background: "#18181b", border: "1px solid #3f3f46", borderRadius: 8 }}
                        formatter={(v) => [formatCurrency(Number(v)), "Receita"]}
                      />
                      <Line type="monotone" dataKey="receita" stroke="#a78bfa" strokeWidth={2} dot={false} />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }}>
            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center gap-2">
                  <Activity className="h-4 w-4 text-emerald-400" />
                  <CardTitle className="text-sm font-display">Ticket Médio</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                {loadTicket ? <Skeleton className="h-48" /> : ticketData.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-12">Sem dados no período.</p>
                ) : (
                  <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={ticketData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#27272a" />
                      <XAxis dataKey="name" tick={{ fontSize: 11, fill: "#71717a" }} />
                      <YAxis tick={{ fontSize: 11, fill: "#71717a" }}
                        tickFormatter={(v) => `R$${v}`} />
                      <Tooltip
                        contentStyle={{ background: "#18181b", border: "1px solid #3f3f46", borderRadius: 8 }}
                        formatter={(v) => [formatCurrency(Number(v)), "Ticket Médio"]}
                      />
                      <Line type="monotone" dataKey="ticket" stroke="#34d399" strokeWidth={2} dot={false} />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </motion.div>
        </div>

        {/* Linha 2: Peças mais alugadas (barra) + Receita por categoria (pizza) */}
        <div className="grid gap-6 lg:grid-cols-2">
          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <BarChart2 className="h-4 w-4 text-amber-400" />
                    <CardTitle className="text-sm font-display">Peças Mais Alugadas</CardTitle>
                  </div>
                  <Button size="sm" variant="ghost" className="h-7 gap-1 text-xs"
                    onClick={() => relatoriosService.exportar("pecas-mais-alugadas", "CSV")}>
                    <Download className="h-3 w-3" />CSV
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                {loadPecas ? <Skeleton className="h-52" /> : pecasData.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-12">Sem dados.</p>
                ) : (
                  <ResponsiveContainer width="100%" height={220}>
                    <BarChart data={pecasData} layout="vertical" margin={{ left: 8 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#27272a" horizontal={false} />
                      <XAxis type="number" tick={{ fontSize: 11, fill: "#71717a" }} />
                      <YAxis type="category" dataKey="name" width={110} tick={{ fontSize: 10, fill: "#a1a1aa" }} />
                      <Tooltip
                        contentStyle={{ background: "#18181b", border: "1px solid #3f3f46", borderRadius: 8 }}
                        formatter={(v) => [v, "Locações"]}
                      />
                      <Bar dataKey="locacoes" fill="#f59e0b" radius={[0, 4, 4, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25 }}>
            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center gap-2">
                  <PieChartIcon className="h-4 w-4 text-violet-400" />
                  <CardTitle className="text-sm font-display">Receita por Categoria</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                {loadCat ? <Skeleton className="h-52" /> : catData.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-12">Sem dados.</p>
                ) : (
                  <ResponsiveContainer width="100%" height={220}>
                    <PieChart>
                      <Pie data={catData} cx="50%" cy="50%" outerRadius={80}
                        dataKey="value" nameKey="name" label={({ name, percent }) =>
                          `${name} ${(percent * 100).toFixed(0)}%`}
                        labelLine={false}
                      >
                        {catData.map((_, i) => (
                          <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={{ background: "#18181b", border: "1px solid #3f3f46", borderRadius: 8 }}
                        formatter={(v) => [formatCurrency(Number(v)), "Receita"]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </motion.div>
        </div>

        {/* Linha 3: Top clientes + Atrasos */}
        <div className="grid gap-6 lg:grid-cols-2">
          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center gap-2">
                  <Trophy className="h-4 w-4 text-amber-400" />
                  <CardTitle className="text-sm font-display">Top Clientes por Receita</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                {loadClientes ? (
                  <div className="space-y-2">{Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
                ) : (topClientes ?? []).length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-8">Sem dados.</p>
                ) : (
                  <ol className="space-y-2">
                    {(topClientes ?? []).map((c, i) => (
                      <li key={c.clienteId} className="flex items-center gap-3 rounded-lg border border-border p-2.5">
                        <span className={`text-base font-bold w-5 text-center ${
                          i === 0 ? "text-amber-400" : i === 1 ? "text-zinc-300" : i === 2 ? "text-amber-600" : "text-muted-foreground"
                        }`}>{i + 1}</span>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium truncate">{c.nomeCliente}</p>
                          <p className="text-xs text-muted-foreground">{c.totalLocacoes} locaç{c.totalLocacoes !== 1 ? "ões" : "ão"}</p>
                        </div>
                        <span className="text-sm font-semibold text-emerald-400">{formatCurrency(c.receitaTotal)}</span>
                      </li>
                    ))}
                  </ol>
                )}
              </CardContent>
            </Card>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.35 }}>
            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center gap-2">
                  <AlertTriangle className="h-4 w-4 text-amber-400" />
                  <CardTitle className="text-sm font-display">Atrasos Pendentes</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                {loadAtrasos ? (
                  <div className="space-y-2">{Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-14" />)}</div>
                ) : (atrasos?.content ?? []).length === 0 ? (
                  <div className="text-center py-8">
                    <p className="text-2xl mb-1">✓</p>
                    <p className="text-sm text-muted-foreground">Nenhum atraso pendente!</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {(atrasos?.content ?? []).map((a) => (
                      <div key={a.locacaoId} className="rounded-lg border border-amber-500/20 bg-amber-500/5 p-3">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm font-medium">Locação #{a.locacaoId}</p>
                            <p className="text-xs text-muted-foreground">
                              Venceu em {formatDate(a.dataFim)} · {a.diasAtraso}d atraso
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="text-xs text-muted-foreground">Multa est.</p>
                            <p className="text-sm font-semibold text-amber-400">{formatCurrency(a.multaEstimada)}</p>
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
        </div>

      </div>
    </div>
  );
}
