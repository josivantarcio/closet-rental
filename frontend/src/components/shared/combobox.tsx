"use client";

import { useState, useRef, useEffect } from "react";
import { Check, ChevronsUpDown, Loader2 } from "lucide-react";
import { Popover } from "radix-ui";
import { cn } from "@/lib/utils";

export interface ComboboxItem {
  id: number;
  label: string;
  sublabel?: string;
}

interface ComboboxProps {
  items: ComboboxItem[];
  value: number | null;
  onSelect: (id: number) => void;
  placeholder: string;
  loading?: boolean;
  disabled?: boolean;
}

export function Combobox({ items, value, onSelect, placeholder, loading, disabled }: ComboboxProps) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const selected = items.find((i) => i.id === value);

  const filtered = query.length >= 1
    ? items.filter((i) =>
        i.label.toLowerCase().includes(query.toLowerCase()) ||
        (i.sublabel?.toLowerCase().includes(query.toLowerCase()) ?? false)
      )
    : items;

  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 50);
    } else {
      setQuery("");
    }
  }, [open]);

  return (
    <Popover.Root open={open} onOpenChange={setOpen}>
      <Popover.Trigger asChild>
        <button
          type="button"
          disabled={disabled || loading}
          className={cn(
            "border-input flex h-9 w-full items-center justify-between gap-2 rounded-md border bg-transparent px-3 py-2 text-sm shadow-xs transition-colors outline-none",
            "hover:bg-accent/50 focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px]",
            "disabled:cursor-not-allowed disabled:opacity-50",
            !selected && "text-muted-foreground"
          )}
        >
          <span className="truncate">
            {loading ? (
              <span className="flex items-center gap-2">
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
                Carregando...
              </span>
            ) : selected ? (
              selected.label
            ) : (
              placeholder
            )}
          </span>
          <ChevronsUpDown className="h-4 w-4 shrink-0 opacity-50" />
        </button>
      </Popover.Trigger>

      <Popover.Portal>
        <Popover.Content
          className={cn(
            "bg-popover text-popover-foreground z-50 w-[var(--radix-popover-trigger-width)] rounded-md border shadow-md",
            "data-[state=open]:animate-in data-[state=closed]:animate-out",
            "data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0",
            "data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95",
            "data-[side=bottom]:slide-in-from-top-2 data-[side=top]:slide-in-from-bottom-2"
          )}
          sideOffset={4}
          align="start"
        >
          <div className="p-2 border-b border-border">
            <input
              ref={inputRef}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Buscar..."
              className="w-full bg-transparent text-sm outline-none placeholder:text-muted-foreground"
            />
          </div>
          <div className="max-h-60 overflow-y-auto p-1">
            {filtered.length === 0 ? (
              <div className="py-6 text-center text-sm text-muted-foreground">
                Nenhum resultado.
              </div>
            ) : (
              filtered.map((item) => (
                <button
                  key={item.id}
                  type="button"
                  className={cn(
                    "flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-none",
                    "hover:bg-accent hover:text-accent-foreground cursor-pointer",
                    value === item.id && "bg-accent/50"
                  )}
                  onClick={() => {
                    onSelect(item.id);
                    setOpen(false);
                  }}
                >
                  <Check
                    className={cn(
                      "h-3.5 w-3.5 shrink-0",
                      value === item.id ? "opacity-100" : "opacity-0"
                    )}
                  />
                  <div className="flex flex-col items-start min-w-0">
                    <span className="truncate">{item.label}</span>
                    {item.sublabel && (
                      <span className="text-xs text-muted-foreground truncate">{item.sublabel}</span>
                    )}
                  </div>
                </button>
              ))
            )}
          </div>
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
}
