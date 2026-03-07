"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { motion } from "framer-motion";
import { User, CalendarDays, CreditCard, LogOut, Shirt } from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/store/auth.store";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";

const navItems = [
  { href: "/minha-conta", label: "Meu Perfil", icon: User, exact: true },
  { href: "/minha-conta/locacoes", label: "Minhas Locações", icon: CalendarDays },
  { href: "/minha-conta/pagamentos", label: "Meus Pagamentos", icon: CreditCard },
];

export default function ClienteLayout({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const { user, logout } = useAuthStore();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace("/login");
    }
  }, [isAuthenticated, router]);

  if (!isAuthenticated) return null;

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      {/* Sidebar simplificada do portal do cliente */}
      <aside className="flex h-screen w-60 flex-col border-r border-border bg-sidebar">
        <div className="flex items-center gap-3 px-6 py-6">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary">
            <Shirt className="h-5 w-5 text-primary-foreground" />
          </div>
          <div>
            <p className="font-display text-base font-semibold leading-tight text-sidebar-foreground">
              Atelier
            </p>
            <p className="text-[11px] text-muted-foreground">Portal do Cliente</p>
          </div>
        </div>

        <Separator />

        <nav className="flex-1 overflow-y-auto px-3 py-4">
          <ul className="space-y-0.5">
            {navItems.map(({ href, label, icon: Icon, exact }) => {
              const active = exact ? pathname === href : pathname.startsWith(href);
              return (
                <li key={href}>
                  <Link
                    href={href}
                    className={cn(
                      "relative flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors",
                      active
                        ? "text-sidebar-primary-foreground"
                        : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                    )}
                  >
                    {active && (
                      <motion.div
                        layoutId="cliente-sidebar-active"
                        className="absolute inset-0 rounded-md bg-sidebar-primary"
                        transition={{ type: "spring", bounce: 0.2, duration: 0.4 }}
                      />
                    )}
                    <Icon className="relative z-10 h-4 w-4 shrink-0" />
                    <span className="relative z-10">{label}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        <Separator />

        <div className="px-4 py-4">
          <div className="mb-3 px-1">
            <p className="text-xs font-semibold text-sidebar-foreground">{user?.username}</p>
            <p className="text-[11px] text-muted-foreground">Cliente</p>
          </div>
          <Button
            variant="ghost"
            size="sm"
            className="w-full justify-start gap-2 text-muted-foreground hover:text-destructive"
            onClick={logout}
          >
            <LogOut className="h-4 w-4" />
            Sair
          </Button>
        </div>
      </aside>

      <main className="flex flex-1 flex-col overflow-hidden">{children}</main>
    </div>
  );
}
