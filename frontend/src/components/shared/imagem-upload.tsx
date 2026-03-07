"use client";

import { useRef, useState } from "react";
import { ImageIcon, Loader2, Trash2, Upload } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { catalogoService } from "@/lib/services/catalogo.service";

interface ImagemUploadProps {
  produtoId: number;
  imagemAtual?: string | null;
  onSuccess: (novaUrl: string | null) => void;
}

const TIPOS_ACEITOS = ["image/jpeg", "image/png", "image/webp"];
const MAX_BYTES = 5 * 1024 * 1024;

export function ImagemUpload({ produtoId, imagemAtual, onSuccess }: ImagemUploadProps) {
  const [preview, setPreview] = useState<string | null>(imagemAtual ?? null);
  const [isDragging, setIsDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  async function handleFile(file: File) {
    if (!TIPOS_ACEITOS.includes(file.type)) {
      toast.error("Tipo inválido. Use JPEG, PNG ou WebP.");
      return;
    }
    if (file.size > MAX_BYTES) {
      toast.error("Arquivo muito grande. Máximo: 5MB.");
      return;
    }

    // Preview imediato
    const reader = new FileReader();
    reader.onload = (e) => setPreview(e.target?.result as string);
    reader.readAsDataURL(file);

    setUploading(true);
    try {
      const produto = await catalogoService.uploadImagem(produtoId, file);
      onSuccess(produto.imagemUrl ?? null);
      toast.success("Imagem enviada com sucesso!");
    } catch {
      toast.error("Erro ao enviar imagem.");
      setPreview(imagemAtual ?? null);
    } finally {
      setUploading(false);
    }
  }

  async function handleRemover() {
    setUploading(true);
    try {
      await catalogoService.removerImagem(produtoId);
      setPreview(null);
      onSuccess(null);
      toast.success("Imagem removida.");
    } catch {
      toast.error("Erro ao remover imagem.");
    } finally {
      setUploading(false);
    }
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  }

  return (
    <div className="space-y-2">
      {/* Área de drop / preview */}
      <div
        className={`relative rounded-lg border-2 border-dashed transition-colors overflow-hidden
          ${isDragging ? "border-primary bg-primary/10" : "border-border bg-muted/30"}
          ${preview ? "h-48" : "h-36"}`}
        onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
        onClick={() => !preview && inputRef.current?.click()}
        style={{ cursor: preview ? "default" : "pointer" }}
      >
        {uploading && (
          <div className="absolute inset-0 flex items-center justify-center bg-background/70 z-10">
            <Loader2 className="h-6 w-6 animate-spin text-primary" />
          </div>
        )}

        {preview ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={preview}
            alt="Preview"
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="flex flex-col items-center justify-center h-full gap-2 text-muted-foreground">
            <ImageIcon className="h-8 w-8" />
            <p className="text-xs text-center px-4">
              Arraste uma imagem ou clique para selecionar
              <br />
              <span className="text-muted-foreground/60">JPEG, PNG ou WebP · máx. 5MB</span>
            </p>
          </div>
        )}
      </div>

      {/* Botões */}
      <div className="flex gap-2">
        <Button
          type="button"
          size="sm"
          variant="outline"
          className="gap-1.5 flex-1"
          onClick={() => inputRef.current?.click()}
          disabled={uploading}
        >
          <Upload className="h-3.5 w-3.5" />
          {preview ? "Trocar" : "Selecionar"}
        </Button>
        {preview && (
          <Button
            type="button"
            size="sm"
            variant="outline"
            className="gap-1.5 text-destructive hover:text-destructive"
            onClick={handleRemover}
            disabled={uploading}
          >
            <Trash2 className="h-3.5 w-3.5" />
            Remover
          </Button>
        )}
      </div>

      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) handleFile(file);
          e.target.value = "";
        }}
      />
    </div>
  );
}
