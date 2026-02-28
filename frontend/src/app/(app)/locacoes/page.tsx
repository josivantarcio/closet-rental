"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Search, Loader2, ArrowRight } from "lucide-react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { LocacaoBadge } from "@/components/shared/status-badge";
import { locacoesService } from "@/lib/services/locacoes.service";
import { formatCurrency, formatDate } from "@/lib/format";
import { useAuthStore } from "@/store/auth.store";
import type { StatusLocacao } from "@/types";

const schema = z.object({
  clienteId: z.string().min(1, "ID do cliente obrigatório"),
  produtoId: z.string().min(1, "ID do produto obrigatório"),
  dataInicio: z.string().min(1, "Data início obrigatória"),
  dataFim: z.string().min(1, "Data fim obrigatória"),
});

type FormData = z.infer<typeof schema>;

const statusTabs: { value: StatusLocacao | "TODOS"; label: string }[] = [
  { value: "TODOS", label: "Todas" },
  { value: "RESERVADO", label: "Reservadas" },
  { value: "RETIRADO", label: "Retiradas" },
  { value: "DEVOLVIDO", label: "Devolvidas" },
  { value: "CANCELADO", label: "Canceladas" },
];

export default function LocacoesPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<StatusLocacao | "TODOS">("TODOS");
  const [dialogOpen, setDialogOpen] = useState(false);
  const { user } = useAuthStore();
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["locacoes", page, statusFilter],
    queryFn: () =>
      locacoesService.listar(page, 20, statusFilter === "TODOS" ? undefined : statusFilter),
  });

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const criarMutation = useMutation({
    mutationFn: (data: FormData) =>
      locacoesService.criarReserva({
        clienteId: Number(data.clienteId),
        produtoId: Number(data.produtoId),
        dataInicio: data.dataInicio,
        dataFim: data.dataFim,
        operador: user?.username ?? "sistema",
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locacoes"] });
      toast.success("Reserva criada com sucesso!");
      setDialogOpen(false);
      reset();
    },
    onError: () => toast.error("Erro ao criar reserva. Verifique disponibilidade."),
  });

  const retirarMutation = useMutation({
    mutationFn: locacoesService.registrarRetirada,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locacoes"] });
      toast.success("Retirada registrada!");
    },
    onError: () => toast.error("Erro ao registrar retirada."),
  });

  const devolverMutation = useMutation({
    mutationFn: locacoesService.registrarDevolucao,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locacoes"] });
      toast.success("Devolução registrada!");
    },
    onError: () => toast.error("Erro ao registrar devolução."),
  });

  const cancelarMutation = useMutation({
    mutationFn: locacoesService.cancelar,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locacoes"] });
      toast.success("Locação cancelada.");
    },
    onError: () => toast.error("Não é possível cancelar esta locação."),
  });

  const filtered = data?.content.filter((l) =>
    String(l.id).includes(search) ||
    String(l.clienteId).includes(search) ||
    String(l.produtoId).includes(search)
  );

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Locações" subtitle="Gestão do fluxo de reservas" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {/* Filtros */}
        <div className="flex items-center gap-3 flex-wrap">
          <div className="relative flex-1 max-w-xs">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Buscar por ID, cliente, produto..."
              className="pl-9"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <Tabs value={statusFilter} onValueChange={(v) => { setStatusFilter(v as StatusLocacao | "TODOS"); setPage(0); }}>
            <TabsList>
              {statusTabs.map((t) => (
                <TabsTrigger key={t.value} value={t.value} className="text-xs">
                  {t.label}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>
          <Button onClick={() => { reset(); setDialogOpen(true); }} className="gap-2 ml-auto">
            <Plus className="h-4 w-4" />
            Nova Reserva
          </Button>
        </div>

        <div className="rounded-lg border border-border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">#</TableHead>
                <TableHead>Cliente</TableHead>
                <TableHead>Produto</TableHead>
                <TableHead>Período</TableHead>
                <TableHead>Total</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="w-32">Ações</TableHead>
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
              ) : (filtered ?? []).length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground py-10">
                    Nenhuma locação encontrada.
                  </TableCell>
                </TableRow>
              ) : (
                (filtered ?? []).map((loc) => (
                  <TableRow key={loc.id}>
                    <TableCell className="font-mono text-xs text-muted-foreground">
                      #{loc.id}
                    </TableCell>
                    <TableCell className="text-sm">Cliente #{loc.clienteId}</TableCell>
                    <TableCell className="text-sm">Produto #{loc.produtoId}</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {formatDate(loc.dataInicio)} → {formatDate(loc.dataFim)}
                    </TableCell>
                    <TableCell className="font-medium">
                      {formatCurrency(loc.valorTotal)}
                    </TableCell>
                    <TableCell><LocacaoBadge status={loc.status} /></TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        {loc.status === "RESERVADO" && (
                          <>
                            <Button
                              variant="outline"
                              size="sm"
                              className="h-7 text-xs"
                              onClick={() => retirarMutation.mutate(loc.id)}
                              disabled={retirarMutation.isPending}
                            >
                              Retirar
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-7 text-xs text-muted-foreground"
                              onClick={() => cancelarMutation.mutate(loc.id)}
                            >
                              Cancelar
                            </Button>
                          </>
                        )}
                        {loc.status === "RETIRADO" && (
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-7 text-xs"
                            onClick={() => devolverMutation.mutate(loc.id)}
                            disabled={devolverMutation.isPending}
                          >
                            Devolver
                          </Button>
                        )}
                        <Button variant="ghost" size="icon" className="h-7 w-7" asChild>
                          <Link href={`/locacoes/${loc.id}`}>
                            <ArrowRight className="h-3.5 w-3.5" />
                          </Link>
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-center gap-2">
            <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
              Anterior
            </Button>
            <span className="text-sm text-muted-foreground">{page + 1} / {data.totalPages}</span>
            <Button variant="outline" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>
              Próxima
            </Button>
          </div>
        )}
      </div>

      {/* Dialog nova reserva */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="font-display">Nova Reserva</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit((d) => criarMutation.mutate(d))} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="clienteId">ID do Cliente</Label>
                <Input id="clienteId" type="number" placeholder="1" {...register("clienteId")} />
                {errors.clienteId && <p className="text-xs text-destructive">{errors.clienteId.message}</p>}
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="produtoId">ID do Produto</Label>
                <Input id="produtoId" type="number" placeholder="1" {...register("produtoId")} />
                {errors.produtoId && <p className="text-xs text-destructive">{errors.produtoId.message}</p>}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="dataInicio">Data Início</Label>
                <Input id="dataInicio" type="date" {...register("dataInicio")} />
                {errors.dataInicio && <p className="text-xs text-destructive">{errors.dataInicio.message}</p>}
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="dataFim">Data Fim</Label>
                <Input id="dataFim" type="date" {...register("dataFim")} />
                {errors.dataFim && <p className="text-xs text-destructive">{errors.dataFim.message}</p>}
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={isSubmitting || criarMutation.isPending}>
                {(isSubmitting || criarMutation.isPending) && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Reservar
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
