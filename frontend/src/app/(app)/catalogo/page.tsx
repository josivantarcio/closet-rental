"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { Plus, Search, Pencil, Trash2, Loader2, LayoutGrid, List } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { DisponibilidadeBadge } from "@/components/shared/status-badge";
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
import { catalogoService } from "@/lib/services/catalogo.service";
import { formatCurrency, categoriaLabel } from "@/lib/format";
import type { ProdutoResponse, ProdutoRequest, CategoriaProduto } from "@/types";

const schema = z.object({
  nome: z.string().min(2, "Nome deve ter ao menos 2 caracteres"),
  categoria: z.enum(["VESTIDO", "TERNO", "ACESSORIO", "CALCADO", "OUTRO"] as const),
  valorDiaria: z.string().refine((v) => !isNaN(Number(v)) && Number(v) > 0, "Valor deve ser positivo"),
  codigo: z.string().optional(),
});

type FormData = {
  nome: string;
  categoria: CategoriaProduto;
  valorDiaria: string;
  codigo?: string;
};

const categorias: CategoriaProduto[] = ["VESTIDO", "TERNO", "ACESSORIO", "CALCADO", "OUTRO"];

type ViewMode = "grid" | "list";

export default function CatalogoPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [viewMode, setViewMode] = useState<ViewMode>("grid");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingProduto, setEditingProduto] = useState<ProdutoResponse | null>(null);
  const queryClient = useQueryClient();

  // Busca no backend quando há texto — usa size grande para cobrir todos os resultados
  const { data, isLoading } = useQuery({
    queryKey: ["catalogo", page, search],
    queryFn: () => catalogoService.listar(search ? 0 : page, search ? 200 : 12),
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
    reset({ nome: "", valorDiaria: "", codigo: "" });
    setDialogOpen(true);
  };

  const openEdit = (produto: ProdutoResponse) => {
    setEditingProduto(produto);
    reset({
      nome: produto.nome,
      categoria: produto.categoria as CategoriaProduto,
      valorDiaria: String(produto.valorDiaria),
      codigo: produto.codigo ?? "",
    });
    setDialogOpen(true);
  };

  const onSubmit = (data: FormData) => {
    const payload = {
      nome: data.nome,
      categoria: data.categoria,
      valorDiaria: Number(data.valorDiaria),
      codigo: data.codigo || undefined,
    };
    if (editingProduto) {
      atualizarMutation.mutate({ id: editingProduto.id, data: payload });
    } else {
      criarMutation.mutate(payload);
    }
  };

  // Filtragem local apenas complementa a busca já feita no backend
  const produtos = (data?.content ?? []).filter((p) =>
    p.nome.toLowerCase().includes(search.toLowerCase()) ||
    (p.codigo?.toLowerCase().includes(search.toLowerCase()) ?? false)
  );

  const showPagination = !search && data && data.totalPages > 1;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Catálogo" subtitle="Gestão de peças para aluguel" />

      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {/* Toolbar */}
        <div className="flex items-center gap-3">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Buscar por nome ou código..."
              className="pl-9"
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
            />
          </div>

          {/* Toggle grid / lista */}
          <div className="flex items-center border border-border rounded-md overflow-hidden">
            <button
              type="button"
              onClick={() => setViewMode("grid")}
              className={`p-2 transition-colors ${viewMode === "grid" ? "bg-primary text-primary-foreground" : "hover:bg-accent text-muted-foreground"}`}
              title="Visualização em grid"
            >
              <LayoutGrid className="h-4 w-4" />
            </button>
            <button
              type="button"
              onClick={() => setViewMode("list")}
              className={`p-2 transition-colors ${viewMode === "list" ? "bg-primary text-primary-foreground" : "hover:bg-accent text-muted-foreground"}`}
              title="Visualização em lista"
            >
              <List className="h-4 w-4" />
            </button>
          </div>

          <Button onClick={openCreate} className="gap-2">
            <Plus className="h-4 w-4" />
            Novo Produto
          </Button>
        </div>

        {/* Contagem */}
        {!isLoading && (
          <p className="text-xs text-muted-foreground">
            {search
              ? `${produtos.length} resultado(s) para "${search}"`
              : `${data?.totalElements ?? 0} peça(s) no catálogo`}
          </p>
        )}

        {/* Conteúdo */}
        {isLoading ? (
          viewMode === "grid" ? (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {Array.from({ length: 8 }).map((_, i) => (
                <Skeleton key={i} className="h-40 rounded-xl" />
              ))}
            </div>
          ) : (
            <div className="rounded-lg border border-border overflow-hidden">
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="flex items-center gap-4 px-4 py-3 border-b border-border last:border-0">
                  <Skeleton className="h-4 w-48" />
                  <Skeleton className="h-4 w-20" />
                  <Skeleton className="h-4 w-24 ml-auto" />
                </div>
              ))}
            </div>
          )
        ) : produtos.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
            <Search className="h-10 w-10 mb-3 opacity-30" />
            <p className="text-sm">Nenhum produto encontrado.</p>
          </div>
        ) : viewMode === "grid" ? (
          /* ── GRID ── */
          <motion.div
            className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
          >
            {produtos.map((produto) => (
              <Card key={produto.id} className="group relative overflow-hidden">
                <CardContent className="pt-5 pb-4 px-4">
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-sm leading-tight truncate">{produto.nome}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {categoriaLabel[produto.categoria] ?? produto.categoria}
                        {produto.codigo && (
                          <span className="ml-1.5 font-mono bg-muted px-1 rounded text-[10px]">
                            {produto.codigo}
                          </span>
                        )}
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
        ) : (
          /* ── LISTA ── */
          <div className="rounded-lg border border-border overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-16">#</TableHead>
                  <TableHead>Nome</TableHead>
                  <TableHead>Categoria</TableHead>
                  <TableHead>Código</TableHead>
                  <TableHead>Valor/dia</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="w-24">Ações</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {produtos.map((produto) => (
                  <TableRow key={produto.id}>
                    <TableCell className="font-mono text-xs text-muted-foreground">
                      #{produto.id}
                    </TableCell>
                    <TableCell className="font-medium">{produto.nome}</TableCell>
                    <TableCell>
                      <Badge variant="outline" className="text-xs">
                        {categoriaLabel[produto.categoria] ?? produto.categoria}
                      </Badge>
                    </TableCell>
                    <TableCell className="font-mono text-xs text-muted-foreground">
                      {produto.codigo ?? "—"}
                    </TableCell>
                    <TableCell className="font-semibold text-primary">
                      {formatCurrency(produto.valorDiaria)}
                    </TableCell>
                    <TableCell>
                      <DisponibilidadeBadge disponivel={produto.disponivel} />
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7"
                          onClick={() => openEdit(produto)}
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-muted-foreground hover:text-destructive"
                          onClick={() => {
                            if (confirm(`Remover "${produto.nome}"?`))
                              deletarMutation.mutate(produto.id);
                          }}
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        {/* Paginação — oculta durante busca */}
        {showPagination && (
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
              <Label htmlFor="codigo">
                Código <span className="text-muted-foreground text-xs">(opcional)</span>
              </Label>
              <Input id="codigo" placeholder="Ex: VES-001-P" {...register("codigo")} />
              <p className="text-xs text-muted-foreground">
                Identifica o modelo — útil quando há múltiplos tamanhos
              </p>
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
