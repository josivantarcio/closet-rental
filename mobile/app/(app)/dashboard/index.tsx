import { useQuery } from "@tanstack/react-query";
import { router } from "expo-router";
import { ScrollView, Text, TouchableOpacity, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { locacoesService } from "@/lib/services/locacoes.service";
import { relatoriosService } from "@/lib/services/relatorios.service";
import { catalogoService } from "@/lib/services/catalogo.service";
import { useAuthStore } from "@/store/auth.store";

function KpiCard({ icon, label, value, color, onPress }: { icon: string; label: string; value: string | number; color: string; onPress?: () => void }) {
  return (
    <TouchableOpacity onPress={onPress} className="flex-1" disabled={!onPress}>
      <Card className="gap-2 items-center py-5">
        <Text style={{ fontSize: 28 }}>{icon}</Text>
        <Text className={`text-2xl font-bold ${color}`}>{value}</Text>
        <Text className="text-white/50 text-xs text-center">{label}</Text>
      </Card>
    </TouchableOpacity>
  );
}

export default function DashboardScreen() {
  const { username, isAdmin } = useAuthStore();

  const hoje = new Date();
  const inicioMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1).toISOString().split("T")[0];
  const fimMes = hoje.toISOString().split("T")[0];

  const { data: retirados } = useQuery({
    queryKey: ["locacoes", "RETIRADO"],
    queryFn: () => locacoesService.listar(0, "RETIRADO"),
  });

  const { data: reservados } = useQuery({
    queryKey: ["locacoes", "RESERVADO"],
    queryFn: () => locacoesService.listar(0, "RESERVADO"),
  });

  const { data: faturamento } = useQuery({
    queryKey: ["relatorios", "faturamento", inicioMes],
    queryFn: () => relatoriosService.faturamento(inicioMes, fimMes),
    enabled: isAdmin(),
  });

  const { data: catalogo } = useQuery({
    queryKey: ["catalogo"],
    queryFn: () => catalogoService.listar(0, 1),
  });

  const receitaMes = faturamento?.reduce((acc, m) => acc + m.receitaBruta, 0) ?? 0;

  return (
    <ScrollView className="flex-1 bg-surface" contentContainerStyle={{ padding: 16, gap: 16 }}>
      {/* Header */}
      <View className="pt-12 pb-2">
        <Text className="text-white/50 text-sm">Bem-vindo,</Text>
        <Text className="text-white text-2xl font-bold">{username}</Text>
      </View>

      {/* KPIs */}
      <View className="flex-row gap-3">
        <KpiCard
          icon="👗"
          label="Peças em uso"
          value={retirados?.totalElements ?? "—"}
          color="text-yellow-400"
          onPress={() => router.push("/(app)/locacoes")}
        />
        <KpiCard
          icon="📋"
          label="Reservas abertas"
          value={reservados?.totalElements ?? "—"}
          color="text-blue-400"
          onPress={() => router.push("/(app)/locacoes")}
        />
      </View>

      {isAdmin() && (
        <View className="flex-row gap-3">
          <KpiCard
            icon="💰"
            label="Receita do mês"
            value={`R$ ${receitaMes.toFixed(0)}`}
            color="text-green-400"
            onPress={() => router.push("/(app)/relatorios")}
          />
          <KpiCard
            icon="📦"
            label="Total catálogo"
            value={catalogo?.totalElements ?? "—"}
            color="text-primary"
            onPress={() => router.push("/(app)/catalogo")}
          />
        </View>
      )}

      {/* Ações rápidas */}
      <Text className="text-white font-semibold text-lg mt-2">Ações rápidas</Text>
      <View className="gap-3">
        {[
          { icon: "➕", label: "Nova reserva", route: "/(app)/locacoes" },
          { icon: "🔄", label: "Registrar retirada / devolução", route: "/(app)/locacoes" },
          { icon: "💳", label: "Processar pagamento", route: "/(app)/pagamentos" },
          { icon: "👤", label: "Cadastrar cliente", route: "/(app)/clientes" },
        ].map((item) => (
          <TouchableOpacity
            key={item.label}
            onPress={() => router.push(item.route as any)}
            className="bg-surface-card border border-border rounded-2xl px-4 py-4 flex-row items-center gap-3"
          >
            <Text style={{ fontSize: 22 }}>{item.icon}</Text>
            <Text className="text-white font-medium flex-1">{item.label}</Text>
            <Text className="text-white/30">›</Text>
          </TouchableOpacity>
        ))}
      </View>
    </ScrollView>
  );
}
