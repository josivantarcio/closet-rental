"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { Shirt, ArrowLeft, Search } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <div className="flex min-h-screen items-center justify-center gradient-luxury px-4">
      {/* Decoração de fundo */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -left-40 -top-40 h-96 w-96 rounded-full bg-primary/5 blur-3xl" />
        <div className="absolute -bottom-40 -right-40 h-96 w-96 rounded-full bg-accent/5 blur-3xl" />
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

        {/* Código de erro */}
        <div>
          <motion.p
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.1, duration: 0.4 }}
            className="font-display text-8xl font-bold text-primary/20 select-none leading-none"
          >
            404
          </motion.p>
        </div>

        {/* Ícone e mensagem */}
        <div className="flex flex-col items-center gap-2">
          <Search className="h-8 w-8 text-muted-foreground" />
          <h1 className="font-display text-2xl font-bold text-foreground">
            Página não encontrada
          </h1>
          <p className="max-w-sm text-sm text-muted-foreground">
            A página que você está procurando não existe ou foi movida para outro endereço.
          </p>
        </div>

        {/* Ações */}
        <div className="flex flex-col gap-3 sm:flex-row">
          <Button asChild>
            <Link href="/dashboard">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Voltar ao painel
            </Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href="/catalogo">Ver catálogo</Link>
          </Button>
        </div>
      </motion.div>
    </div>
  );
}
