"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { Plus, Search, Pencil, Trash2, Loader2 } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { DisponibilidadeBadge } from "@/components/shared/status-badge";
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
import { catalogoService } from "@/lib/services/catalogo.service";
import { formatCurrency, categoriaLabel } from "@/lib/format";
import type { ProdutoResponse, ProdutoRequest, CategoriaProduto } from "@/types";

const schema = z.object({
  nome: z.string().min(2, "Nome deve ter ao menos 2 caracteres"),
  categoria: z.enum(["VESTIDO", "TERNO", "ACESSORIO", "CALCADO", "OUTRO"] as const),
  valorDiaria: z.string().refine((v) => !isNaN(Number(v)) && Number(v) > 0, "Valor deve ser positivo"),
});

type FormData = {
  nome: string;
  categoria: CategoriaProduto;
  valorDiaria: string;
};

const categorias: CategoriaProduto[] = ["VESTIDO", "TERNO", "ACESSORIO", "CALCADO", "OUTRO"];

export default function CatalogoPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingProduto, setEditingProduto] = useState<ProdutoResponse | null>(null);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["catalogo", page],
    queryFn: () => catalogoService.listar(page, 12),
  });

  const { register, handleSubmit, setValue, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const criarMutation = useMutation({
    mutationFn: catalogoService.criar,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["catalogo"] });
      toast.success("Produto criado com sucesso!");
      setDialogOpen(false);
      reset();
    },
    onError: () => toast.error("Erro ao criar produto."),
  });

  const atualizarMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ProdutoRequest }) =>
      catalogoService.atualizar(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["catalogo"] });
      toast.success("Produto atualizado!");
      setDialogOpen(false);
      setEditingProduto(null);
      reset();
    },
    onError: () => toast.error("Erro ao atualizar produto."),
  });

  const deletarMutation = useMutation({
    mutationFn: catalogoService.deletar,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["catalogo"] });
      toast.success("Produto removido.");
    },
    onError: () => toast.error("Não é possível remover produto com locações ativas."),
  });

  const openCreate = () => {
    setEditingProduto(null);
    reset({ nome: "", valorDiaria: "" });
    setDialogOpen(true);
  };

  const openEdit = (produto: ProdutoResponse) => {
    setEditingProduto(produto);
    reset({
      nome: produto.nome,
      categoria: produto.categoria as CategoriaProduto,
      valorDiaria: String(produto.valorDiaria),
    });
    setDialogOpen(true);
  };

  const onSubmit = (data: FormData) => {
    const payload = { ...data, valorDiaria: Number(data.valorDiaria) };
    if (editingProduto) {
      atualizarMutation.mutate({ id: editingProduto.id, data: payload });
    } else {
      criarMutation.mutate(payload);
    }
  };

  const filtered = data?.content.filter((p) =>
    p.nome.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Catálogo" subtitle="Gestão de peças para aluguel" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {/* Ações */}
        <div className="flex items-center gap-3">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Buscar produto..."
              className="pl-9"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <Button onClick={openCreate} className="gap-2">
            <Plus className="h-4 w-4" />
            Novo Produto
          </Button>
        </div>

        {/* Grid */}
        {isLoading ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <Skeleton key={i} className="h-40 rounded-xl" />
            ))}
          </div>
        ) : (
          <>
            <motion.div
              className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              {(filtered ?? []).map((produto) => (
                <Card key={produto.id} className="group relative overflow-hidden">
                  <CardContent className="pt-5 pb-4 px-4">
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-sm leading-tight truncate">{produto.nome}</p>
                        <p className="text-xs text-muted-foreground mt-0.5">
                          {categoriaLabel[produto.categoria] ?? produto.categoria}
                        </p>
                      </div>
                      <DisponibilidadeBadge disponivel={produto.disponivel} />
                    </div>
                    <p className="mt-3 text-lg font-bold font-display text-primary">
                      {formatCurrency(produto.valorDiaria)}
                      <span className="text-xs font-normal text-muted-foreground">/dia</span>
                    </p>
                    <div className="mt-3 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <Button
                        variant="outline"
                        size="sm"
                        className="flex-1 h-7 text-xs gap-1"
                        onClick={() => openEdit(produto)}
                      >
                        <Pencil className="h-3 w-3" /> Editar
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        className="h-7 w-7 p-0 text-destructive hover:text-destructive"
                        onClick={() => {
                          if (confirm(`Remover "${produto.nome}"?`))
                            deletarMutation.mutate(produto.id);
                        }}
                      >
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </motion.div>

            {/* Paginação simples */}
            {data && data.totalPages > 1 && (
              <div className="flex items-center justify-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Anterior
                </Button>
                <span className="text-sm text-muted-foreground">
                  {page + 1} / {data.totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page >= data.totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Próxima
                </Button>
              </div>
            )}
          </>
        )}
      </div>

      {/* Dialog criar/editar */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="font-display">
              {editingProduto ? "Editar Produto" : "Novo Produto"}
            </DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="nome">Nome</Label>
              <Input id="nome" placeholder="Ex: Vestido Longo Azul" {...register("nome")} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label>Categoria</Label>
              <Select
                defaultValue={editingProduto?.categoria}
                onValueChange={(v) => setValue("categoria", v as CategoriaProduto)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione..." />
                </SelectTrigger>
                <SelectContent>
                  {categorias.map((c) => (
                    <SelectItem key={c} value={c}>
                      {categoriaLabel[c] ?? c}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.categoria && (
                <p className="text-xs text-destructive">{errors.categoria.message}</p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="valorDiaria">Valor Diária (R$)</Label>
              <Input
                id="valorDiaria"
                type="number"
                step="0.01"
                min="0"
                placeholder="0.00"
                {...register("valorDiaria")}
              />
              {errors.valorDiaria && (
                <p className="text-xs text-destructive">{errors.valorDiaria.message}</p>
              )}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {editingProduto ? "Salvar" : "Criar"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
