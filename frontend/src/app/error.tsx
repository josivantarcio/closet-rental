"use client";

import { useEffect } from "react";
import { motion } from "framer-motion";
import { Shirt, RotateCcw, ArrowLeft, AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import Link from "next/link";

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function Error({ error, reset }: ErrorProps) {
  useEffect(() => {
    console.error("[Error Boundary]", error);
  }, [error]);

  return (
    <div className="flex min-h-screen items-center justify-center gradient-luxury px-4">
      {/* Decoração de fundo */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -left-40 -top-40 h-96 w-96 rounded-full bg-destructive/5 blur-3xl" />
        <div className="absolute -bottom-40 -right-40 h-96 w-96 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="flex flex-col items-center gap-6 text-center"
      >
        {/* Logo */}
        <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary shadow-lg shadow-primary/20">
          <Shirt className="h-7 w-7 text-primary-foreground" />
        </div>

        {/* Ícone de erro */}
        <div className="flex h-16 w-16 items-center justify-center rounded-full bg-destructive/10 border border-destructive/20">
          <AlertTriangle className="h-8 w-8 text-destructive" />
        </div>

        {/* Mensagem */}
        <div className="flex flex-col items-center gap-2">
          <h1 className="font-display text-2xl font-bold text-foreground">
            Algo deu errado
          </h1>
          <p className="max-w-sm text-sm text-muted-foreground">
            Ocorreu um erro inesperado. Tente novamente ou volte ao painel principal.
          </p>
          {error.digest && (
            <p className="mt-1 font-mono text-xs text-muted-foreground/60">
              ID: {error.digest}
            </p>
          )}
        </div>

        {/* Ações */}
        <div className="flex flex-col gap-3 sm:flex-row">
          <Button onClick={reset}>
            <RotateCcw className="mr-2 h-4 w-4" />
            Tentar novamente
          </Button>
          <Button variant="outline" asChild>
            <Link href="/dashboard">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Voltar ao painel
            </Link>
          </Button>
        </div>
      </motion.div>
    </div>
  );
}
