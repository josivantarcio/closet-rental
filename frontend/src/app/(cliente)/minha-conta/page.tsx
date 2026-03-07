"use client";

import { useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { User, Mail, Shield, Calendar, Key, Loader2 } from "lucide-react";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { minhaContaService } from "@/lib/services/minhaConta.service";
import { formatDate } from "@/lib/format";

const senhaSchema = z
  .object({
    senhaAtual: z.string().min(1, "Senha atual é obrigatória"),
    novaSenha: z.string().min(6, "Nova senha deve ter no mínimo 6 caracteres"),
    confirmarSenha: z.string().min(1, "Confirme a nova senha"),
  })
  .refine((d) => d.novaSenha === d.confirmarSenha, {
    message: "As senhas não coincidem",
    path: ["confirmarSenha"],
  });

type SenhaForm = z.infer<typeof senhaSchema>;

export default function MinhaContaPage() {
  const [editingSenha, setEditingSenha] = useState(false);

  const { data: perfil, isLoading } = useQuery({
    queryKey: ["minha-conta-perfil"],
    queryFn: minhaContaService.getPerfil,
    staleTime: 60_000,
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<SenhaForm>({ resolver: zodResolver(senhaSchema) });

  const alterarSenha = useMutation({
    mutationFn: (data: SenhaForm) =>
      minhaContaService.alterarSenha({ senhaAtual: data.senhaAtual, novaSenha: data.novaSenha }),
    onSuccess: () => {
      toast.success("Senha alterada com sucesso");
      reset();
      setEditingSenha(false);
    },
    onError: () => toast.error("Erro ao alterar senha. Verifique a senha atual."),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Meu Perfil" subtitle="Dados da sua conta" />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto space-y-6">
          {/* Dados do perfil */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <User className="h-4 w-4" />
                Informações da Conta
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {isLoading ? (
                <div className="space-y-3">
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-3/4" />
                  <Skeleton className="h-4 w-1/2" />
                </div>
              ) : (
                <dl className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <dt className="text-muted-foreground flex items-center gap-1">
                      <User className="h-3 w-3" /> Nome
                    </dt>
                    <dd className="font-medium mt-0.5">{perfil?.nome}</dd>
                  </div>
                  <div>
                    <dt className="text-muted-foreground">Username</dt>
                    <dd className="font-medium mt-0.5 font-mono text-xs">{perfil?.username}</dd>
                  </div>
                  <div>
                    <dt className="text-muted-foreground flex items-center gap-1">
                      <Mail className="h-3 w-3" /> E-mail
                    </dt>
                    <dd className="font-medium mt-0.5">{perfil?.email}</dd>
                  </div>
                  <div>
                    <dt className="text-muted-foreground flex items-center gap-1">
                      <Shield className="h-3 w-3" /> Perfil
                    </dt>
                    <dd className="font-medium mt-0.5">{perfil?.role}</dd>
                  </div>
                  <div>
                    <dt className="text-muted-foreground flex items-center gap-1">
                      <Calendar className="h-3 w-3" /> Membro desde
                    </dt>
                    <dd className="font-medium mt-0.5">
                      {perfil?.createdAt ? formatDate(perfil.createdAt.split("T")[0]) : "—"}
                    </dd>
                  </div>
                </dl>
              )}
            </CardContent>
          </Card>

          {/* Alterar senha */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2 text-base">
                  <Key className="h-4 w-4" />
                  Segurança
                </CardTitle>
                {!editingSenha && (
                  <Button variant="outline" size="sm" onClick={() => setEditingSenha(true)}>
                    Alterar senha
                  </Button>
                )}
              </div>
            </CardHeader>
            {editingSenha && (
              <CardContent>
                <Separator className="mb-4" />
                <form
                  onSubmit={handleSubmit((d) => alterarSenha.mutate(d))}
                  className="space-y-4"
                >
                  <div className="space-y-1">
                    <Label>Senha atual</Label>
                    <Input type="password" {...register("senhaAtual")} />
                    {errors.senhaAtual && (
                      <p className="text-xs text-destructive">{errors.senhaAtual.message}</p>
                    )}
                  </div>
                  <div className="space-y-1">
                    <Label>Nova senha</Label>
                    <Input type="password" {...register("novaSenha")} />
                    {errors.novaSenha && (
                      <p className="text-xs text-destructive">{errors.novaSenha.message}</p>
                    )}
                  </div>
                  <div className="space-y-1">
                    <Label>Confirmar nova senha</Label>
                    <Input type="password" {...register("confirmarSenha")} />
                    {errors.confirmarSenha && (
                      <p className="text-xs text-destructive">{errors.confirmarSenha.message}</p>
                    )}
                  </div>
                  <div className="flex gap-2 pt-2">
                    <Button type="submit" size="sm" disabled={alterarSenha.isPending}>
                      {alterarSenha.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                      Salvar
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        reset();
                        setEditingSenha(false);
                      }}
                    >
                      Cancelar
                    </Button>
                  </div>
                </form>
              </CardContent>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
