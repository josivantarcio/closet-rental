"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Loader2, RotateCcw } from "lucide-react";
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
import { PagamentoBadge } from "@/components/shared/status-badge";
import { pagamentosService } from "@/lib/services/pagamentos.service";
import { formatCurrency, formatDateTime, metodoPagamentoLabel } from "@/lib/format";
import type { MetodoPagamento } from "@/types";

const schema = z.object({
  locacaoId: z.string().min(1, "ID da locação obrigatório"),
  valor: z.string().min(1, "Valor obrigatório"),
  metodo: z.enum(["PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "DINHEIRO"] as const),
});

type FormData = z.infer<typeof schema>;

const metodos: MetodoPagamento[] = ["PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "DINHEIRO"];

export default function PagamentosPage() {
  const [page, setPage] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["pagamentos", page],
    queryFn: () => pagamentosService.listar(page, 20),
  });

  const { register, handleSubmit, setValue, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const processarMutation = useMutation({
    mutationFn: pagamentosService.processar,
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["pagamentos"] });
      if (result.status === "APROVADO") {
        toast.success("Pagamento aprovado!");
      } else {
        toast.error("Pagamento falhou. Tente outro método.");
      }
      setDialogOpen(false);
      reset();
    },
    onError: () => toast.error("Erro ao processar pagamento."),
  });

  const estornarMutation = useMutation({
    mutationFn: (id: number) => pagamentosService.estornar(id, "Estorno solicitado pelo operador"),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pagamentos"] });
      toast.success("Pagamento estornado.");
    },
    onError: () => toast.error("Não é possível estornar este pagamento."),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Pagamentos" subtitle="Processamento e histórico" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        <div className="flex justify-end">
          <Button onClick={() => { reset(); setDialogOpen(true); }} className="gap-2">
            <Plus className="h-4 w-4" />
            Processar Pagamento
          </Button>
        </div>

        <div className="rounded-lg border border-border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">#</TableHead>
                <TableHead>Locação</TableHead>
                <TableHead>Valor</TableHead>
                <TableHead>Método</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Data</TableHead>
                <TableHead className="w-20">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <TableRow key={i}>
                    {Array.from({ length: 7 }).map((_, j) => (
                      <TableCell key={j}><Skeleton className="h-4 w-full" /></TableCell>
                    ))}
                  </TableRow>
                ))
              ) : (data?.content ?? []).length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground py-10">
                    Nenhum pagamento registrado.
                  </TableCell>
                </TableRow>
              ) : (
                (data?.content ?? []).map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-mono text-xs text-muted-foreground">#{p.id}</TableCell>
                    <TableCell>Locação #{p.locacaoId}</TableCell>
                    <TableCell className="font-semibold">{formatCurrency(p.valor)}</TableCell>
                    <TableCell className="text-sm">{metodoPagamentoLabel[p.metodo]}</TableCell>
                    <TableCell><PagamentoBadge status={p.status} /></TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {p.processadoEm ? formatDateTime(p.processadoEm) : "—"}
                    </TableCell>
                    <TableCell>
                      {p.status === "APROVADO" && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-muted-foreground hover:text-purple-400"
                          onClick={() => {
                            if (confirm("Estornar este pagamento?")) estornarMutation.mutate(p.id);
                          }}
                        >
                          <RotateCcw className="h-3.5 w-3.5" />
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
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

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-sm">
          <DialogHeader>
            <DialogTitle className="font-display">Processar Pagamento</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit((d) => processarMutation.mutate({ locacaoId: Number(d.locacaoId), valor: Number(d.valor), metodo: d.metodo }))} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="locacaoId">ID da Locação</Label>
              <Input id="locacaoId" type="number" placeholder="1" {...register("locacaoId")} />
              {errors.locacaoId && <p className="text-xs text-destructive">{errors.locacaoId.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="valor">Valor (R$)</Label>
              <Input id="valor" type="number" step="0.01" placeholder="0.00" {...register("valor")} />
              {errors.valor && <p className="text-xs text-destructive">{errors.valor.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label>Método de Pagamento</Label>
              <Select onValueChange={(v) => setValue("metodo", v as MetodoPagamento)}>
                <SelectTrigger><SelectValue placeholder="Selecione..." /></SelectTrigger>
                <SelectContent>
                  {metodos.map((m) => (
                    <SelectItem key={m} value={m}>{metodoPagamentoLabel[m]}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.metodo && <p className="text-xs text-destructive">{errors.metodo.message}</p>}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>Cancelar</Button>
              <Button type="submit" disabled={isSubmitting || processarMutation.isPending}>
                {(isSubmitting || processarMutation.isPending) && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Processar
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
