"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ChevronLeft, ChevronRight, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { catalogoService } from "@/lib/services/catalogo.service";
import type { PeriodoOcupadoResponse } from "@/types";

interface AgendaCalendarioProps {
  produtoId: number;
  nomeProduto: string;
}

function toYearMonth(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

const MESES = [
  "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
  "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
];

const SEMANA = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];

function classifyDay(
  day: number,
  year: number,
  month: number,
  periodos: PeriodoOcupadoResponse[]
): "livre" | "reservado" | "retirado" {
  const dataStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
  for (const p of periodos) {
    if (dataStr >= p.dataInicio && dataStr <= p.dataFim) {
      return p.status === "RETIRADO" ? "retirado" : "reservado";
    }
  }
  return "livre";
}

export function AgendaCalendario({ produtoId, nomeProduto }: AgendaCalendarioProps) {
  const now = new Date();
  const [ano, setAno] = useState(now.getFullYear());
  const [mes, setMes] = useState(now.getMonth()); // 0-indexed

  const mesParam = toYearMonth(new Date(ano, mes, 1));

  const { data: periodos, isLoading } = useQuery({
    queryKey: ["agenda", produtoId, mesParam],
    queryFn: () => catalogoService.buscarAgenda(produtoId, mesParam),
    staleTime: 60_000,
  });

  const navMes = (delta: number) => {
    const d = new Date(ano, mes + delta, 1);
    setAno(d.getFullYear());
    setMes(d.getMonth());
  };

  // Dias do mês e offset do primeiro dia (domingo = 0)
  const primeiroDia = new Date(ano, mes, 1).getDay();
  const totalDias = new Date(ano, mes + 1, 0).getDate();
  const hoje = new Date();
  const isHoje = (d: number) =>
    d === hoje.getDate() && mes === hoje.getMonth() && ano === hoje.getFullYear();

  return (
    <div className="w-full select-none">
      {/* Header de navegação */}
      <div className="flex items-center justify-between mb-3">
        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => navMes(-1)}>
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <span className="text-sm font-semibold">
          {MESES[mes]} {ano}
        </span>
        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => navMes(1)}>
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>

      {/* Legenda */}
      <div className="flex items-center gap-3 mb-3 justify-center text-[11px] text-muted-foreground">
        <span className="flex items-center gap-1">
          <span className="inline-block w-3 h-3 rounded-sm bg-amber-400/80" /> Reservado
        </span>
        <span className="flex items-center gap-1">
          <span className="inline-block w-3 h-3 rounded-sm bg-rose-500/80" /> Retirado
        </span>
        <span className="flex items-center gap-1">
          <span className="inline-block w-3 h-3 rounded-sm bg-emerald-500/80" /> Livre
        </span>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-8">
          <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
        </div>
      ) : (
        <div className="grid grid-cols-7 gap-0.5">
          {/* Cabeçalho dias da semana */}
          {SEMANA.map((s) => (
            <div key={s} className="text-center text-[10px] font-medium text-muted-foreground py-1">
              {s}
            </div>
          ))}

          {/* Células vazias antes do primeiro dia */}
          {Array.from({ length: primeiroDia }).map((_, i) => (
            <div key={`empty-${i}`} />
          ))}

          {/* Dias */}
          {Array.from({ length: totalDias }).map((_, i) => {
            const dia = i + 1;
            const status = classifyDay(dia, ano, mes, periodos ?? []);

            const bgClass =
              status === "retirado"
                ? "bg-rose-500/80 text-white font-semibold"
                : status === "reservado"
                ? "bg-amber-400/80 text-amber-950 font-semibold"
                : "hover:bg-accent";

            const hojeClass = isHoje(dia) ? "ring-1 ring-primary ring-offset-1" : "";

            return (
              <div
                key={dia}
                title={
                  status === "livre"
                    ? `${dia}/${mes + 1} — Livre`
                    : `${dia}/${mes + 1} — ${status === "reservado" ? "Reservado" : "Retirado"}`
                }
                className={`
                  flex items-center justify-center rounded-sm text-xs py-1 cursor-default transition-colors
                  ${bgClass} ${hojeClass}
                `}
              >
                {dia}
              </div>
            );
          })}
        </div>
      )}

      {/* Sumário de ocupações do mês */}
      {!isLoading && periodos && periodos.length > 0 && (
        <div className="mt-3 space-y-1 border-t border-border pt-3">
          <p className="text-[11px] font-medium text-muted-foreground mb-1">Ocupações neste mês:</p>
          {periodos.map((p) => (
            <div key={p.locacaoId} className="flex items-center justify-between text-[11px]">
              <span className={`px-1.5 py-0.5 rounded text-[10px] font-medium ${
                p.status === "RETIRADO"
                  ? "bg-rose-500/20 text-rose-600"
                  : "bg-amber-400/20 text-amber-700"
              }`}>
                {p.status}
              </span>
              <span className="text-muted-foreground">
                {p.dataInicio.split("-").reverse().join("/")} → {p.dataFim.split("-").reverse().join("/")}
              </span>
              <span className="font-mono text-muted-foreground">#{p.locacaoId}</span>
            </div>
          ))}
        </div>
      )}

      {!isLoading && (!periodos || periodos.length === 0) && (
        <p className="text-center text-xs text-muted-foreground mt-3">
          Nenhuma ocupação em {MESES[mes]}.
        </p>
      )}
    </div>
  );
}
