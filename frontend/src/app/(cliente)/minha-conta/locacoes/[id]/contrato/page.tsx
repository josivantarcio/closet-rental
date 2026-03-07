"use client";

import { useEffect } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { minhaContaService } from "@/lib/services/minhaConta.service";

/**
 * Rota de download de contrato PDF.
 * Faz fetch autenticado (Bearer token via axios) e abre o PDF no navegador.
 */
export default function ContratoPage() {
  const { id } = useParams<{ id: string }>();

  useEffect(() => {
    const download = async () => {
      try {
        const { api } = await import("@/lib/api");
        const response = await api.get(`/v1/minha-conta/locacoes/${id}/contrato`, {
          responseType: "blob",
        });
        const blob = new Blob([response.data], { type: "application/pdf" });
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank");
        // Limpa a URL após 60s
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      } catch {
        window.location.href = "/minha-conta/locacoes";
      }
    };
    download();
  }, [id]);

  return (
    <div className="flex h-full items-center justify-center">
      <div className="flex flex-col items-center gap-3 text-muted-foreground">
        <Loader2 className="h-8 w-8 animate-spin" />
        <p className="text-sm">Gerando contrato PDF…</p>
      </div>
    </div>
  );
}
