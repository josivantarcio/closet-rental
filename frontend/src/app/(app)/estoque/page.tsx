"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Wrench, RotateCcw, Package, PackageCheck, PackageX, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Header } from "@/components/layout/header";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Combobox, type ComboboxItem } from "@/components/shared/combobox";
import { api } from "@/lib/api";
import { catalogoService } from "@/lib/services/catalogo.service";
import { formatDateTime } from "@/lib/format";
import type { MovimentacaoEstoqueResponse, Page, ProdutoResponse } from "@/types";

const tipoConfig: Record<string, { label: string; className: string }> = {
  SAIDA: { label: "Saída", className: "bg-rose-500/10 text-rose-400 border-rose-500/20" },
  ENTRADA: { label: "Entrada", className: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20" },
  MANUTENCAO: { label: "Manutenção", className: "bg-amber-500/10 text-amber-400 border-amber-500/20" },
  RETORNO_MANUTENCAO: { label: "Retorno Manutenção", className: "bg-blue-500/10 text-blue-400 border-blue-500/20" },
};

type DialogMode = "manutencao" | "retorno" | null;

export default function EstoquePage() {
  const [page, setPage] = useState(0);
  const [dialogMode, setDialogMode] = useState<DialogMode>(null);
  const [selectedProdutoId, setSelectedProdutoId] = useState<number | null>(null);
  const [motivo, setMotivo] = useState("");
  const [produtoError, setProdutoError] = useState(false);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["estoque-historico", page],
    queryFn: () =>
      api
        .get<Page<MovimentacaoEstoqueResponse>>("/v1/estoque/historico", {
          params: { page, size: 20 },
        })
        .then((r) => r.data),
  });

  const { data: catalogoData, isLoading: catalogoLoading } = useQuery({
    queryKey: ["catalogo-select"],
    queryFn: () => catalogoService.listar(0, 200),
    staleTime: 60_000,
  });

  const produtoMap = new Map<number, ProdutoResponse>(
    (catalogoData?.content ?? []).map((p) => [p.id, p])
  );

  const totalPecas = catalogoData?.totalElements ?? 0;
  const disponiveis = (catalogoData?.content ?? []).filter((p) => p.disponivel).length;
  const indisponiveis = (catalogoData?.content ?? []).filter((p) => !p.disponivel).length;

  const produtoItems: ComboboxItem[] = (catalogoData?.content ?? []).map((p) => ({
    id: p.id,
    label: p.nome,
    sublabel: p.disponivel ? "Disponível" : "Indisponível",
  }));

  const manutencaoMutation = useMutation({
    mutationFn: ({ produtoId, motivo }: { produtoId: number; motivo: string }) =>
      api.post("/v1/estoque/manutencao", { produtoId, motivo }).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["estoque-historico"] });
      queryClient.invalidateQueries({ queryKey: ["catalogo-select"] });
      toast.success("Peça enviada para manutenção.");
      closeDialog();
    },
    onError: () => toast.error("Erro ao enviar para manutenção. A peça pode já estar indisponível."),
  });

  const retornoMutation = useMutation({
    mutationFn: ({ produtoId, observacoes }: { produtoId: number; observacoes: string }) =>
      api.post("/v1/estoque/manutencao/retorno", { produtoId, observacoes }).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["estoque-historico"] });
      queryClient.invalidateQueries({ queryKey: ["catalogo-select"] });
      toast.success("Retorno da manutenção registrado.");
      closeDialog();
    },
    onError: () => toast.error("Erro ao registrar retorno. A peça pode já estar disponível."),
  });

  const openDialog = (mode: DialogMode) => {
    setDialogMode(mode);
    setSelectedProdutoId(null);
    setMotivo("");
    setProdutoError(false);
  };

  const closeDialog = () => {
    setDialogMode(null);
    setSelectedProdutoId(null);
    setMotivo("");
  };

  const handleConfirm = () => {
    if (!selectedProdutoId) { setProdutoError(true); return; }
    if (dialogMode === "manutencao") {
      manutencaoMutation.mutate({ produtoId: selectedProdutoId, motivo });
    } else if (dialogMode === "retorno") {
      retornoMutation.mutate({ produtoId: selectedProdutoId, observacoes: motivo });
    }
  };

  const isPending = manutencaoMutation.isPending || retornoMutation.isPending;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Estoque" subtitle="Histórico de movimentações" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {/* Summary cards */}
        <div className="grid grid-cols-3 gap-4">
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <Package className="h-8 w-8 text-muted-foreground shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Total de Peças</p>
                <p className="text-2xl font-bold">{catalogoLoading ? "—" : totalPecas}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <PackageCheck className="h-8 w-8 text-emerald-400 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Disponíveis</p>
                <p className="text-2xl font-bold text-emerald-400">{catalogoLoading ? "—" : disponiveis}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <PackageX className="h-8 w-8 text-rose-400 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Indisponíveis</p>
                <p className="text-2xl font-bold text-rose-400">{catalogoLoading ? "—" : indisponiveis}</p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Quick actions */}
        <div className="flex gap-2 justify-end">
          <Button variant="outline" className="gap-2" onClick={() => openDialog("manutencao")}>
            <Wrench className="h-4 w-4" />
            Enviar para Manutenção
          </Button>
          <Button variant="outline" className="gap-2" onClick={() => openDialog("retorno")}>
            <RotateCcw className="h-4 w-4" />
            Retornar da Manutenção
          </Button>
        </div>

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
                  const produto = produtoMap.get(m.produtoId);
                  return (
                    <TableRow key={m.id}>
                      <TableCell className="font-mono text-xs text-muted-foreground">#{m.id}</TableCell>
                      <TableCell className="font-medium">
                        {produto ? produto.nome : `Produto #${m.produtoId}`}
                      </TableCell>
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

      <Dialog open={!!dialogMode} onOpenChange={(open) => { if (!open) closeDialog(); }}>
        <DialogContent className="sm:max-w-sm">
          <DialogHeader>
            <DialogTitle className="font-display">
              {dialogMode === "manutencao" ? "Enviar para Manutenção" : "Retornar da Manutenção"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-1.5">
              <Label>Produto</Label>
              <Combobox
                items={produtoItems}
                value={selectedProdutoId}
                onSelect={(id) => { setSelectedProdutoId(id); setProdutoError(false); }}
                placeholder="Selecionar produto..."
                loading={catalogoLoading}
              />
              {produtoError && <p className="text-xs text-destructive">Produto obrigatório</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="motivo">
                {dialogMode === "manutencao" ? "Motivo" : "Observações"}
              </Label>
              <Input
                id="motivo"
                placeholder={dialogMode === "manutencao" ? "Descreva o problema..." : "Observações do retorno..."}
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={closeDialog}>Cancelar</Button>
            <Button onClick={handleConfirm} disabled={isPending}>
              {isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Confirmar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
