import { useQuery } from "@tanstack/react-query";
import { router } from "expo-router";
import { ScrollView, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { locacoesService } from "@/lib/services/locacoes.service";
import { relatoriosService } from "@/lib/services/relatorios.service";
import { catalogoService } from "@/lib/services/catalogo.service";
import { useAuthStore } from "@/store/auth.store";

function KpiCard({ icon, label, value, color, onPress }: { icon: string; label: string; value: string | number; color: string; onPress?: () => void }) {
  return (
    <TouchableOpacity onPress={onPress} style={styles.kpiTouch} disabled={!onPress}>
      <Card style={styles.kpiCard}>
        <Text style={{ fontSize: 28 }}>{icon}</Text>
        <Text style={[styles.kpiValue, { color }]}>{value}</Text>
        <Text style={styles.kpiLabel}>{label}</Text>
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
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.welcome}>Bem-vindo,</Text>
        <Text style={styles.username}>{username}</Text>
      </View>

      {/* KPIs row 1 */}
      <View style={styles.kpiRow}>
        <KpiCard
          icon="👗"
          label="Peças em uso"
          value={retirados?.totalElements ?? "—"}
          color="#facc15"
          onPress={() => router.push("/(app)/locacoes")}
        />
        <KpiCard
          icon="📋"
          label="Reservas abertas"
          value={reservados?.totalElements ?? "—"}
          color="#60a5fa"
          onPress={() => router.push("/(app)/locacoes")}
        />
      </View>

      {isAdmin() && (
        <View style={styles.kpiRow}>
          <KpiCard
            icon="💰"
            label="Receita do mês"
            value={`R$ ${receitaMes.toFixed(0)}`}
            color="#4ade80"
            onPress={() => router.push("/(app)/relatorios")}
          />
          <KpiCard
            icon="📦"
            label="Total catálogo"
            value={catalogo?.totalElements ?? "—"}
            color="#e91e8c"
            onPress={() => router.push("/(app)/catalogo")}
          />
        </View>
      )}

      {/* Ações rápidas */}
      <Text style={styles.sectionTitle}>Ações rápidas</Text>
      <View style={styles.actionsContainer}>
        {[
          { icon: "➕", label: "Nova reserva", route: "/(app)/locacoes" },
          { icon: "🔄", label: "Registrar retirada / devolução", route: "/(app)/locacoes" },
          { icon: "💳", label: "Processar pagamento", route: "/(app)/pagamentos" },
          { icon: "👤", label: "Cadastrar cliente", route: "/(app)/clientes" },
        ].map((item) => (
          <TouchableOpacity
            key={item.label}
            onPress={() => router.push(item.route as any)}
            style={styles.actionItem}
          >
            <Text style={{ fontSize: 22 }}>{item.icon}</Text>
            <Text style={styles.actionLabel}>{item.label}</Text>
            <Text style={styles.actionChevron}>›</Text>
          </TouchableOpacity>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
    backgroundColor: "#1a1a1a",
  },
  content: {
    padding: 16,
    gap: 16,
  },
  header: {
    paddingTop: 48,
    paddingBottom: 8,
  },
  welcome: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  username: {
    color: "#ffffff",
    fontSize: 24,
    fontWeight: "700",
  },
  kpiRow: {
    flexDirection: "row",
    gap: 12,
  },
  kpiTouch: {
    flex: 1,
  },
  kpiCard: {
    gap: 8,
    alignItems: "center",
    paddingVertical: 20,
  },
  kpiValue: {
    fontSize: 24,
    fontWeight: "700",
  },
  kpiLabel: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 12,
    textAlign: "center",
  },
  sectionTitle: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 18,
    marginTop: 8,
  },
  actionsContainer: {
    gap: 12,
  },
  actionItem: {
    backgroundColor: "#2a2a2a",
    borderWidth: 1,
    borderColor: "#3a3a3a",
    borderRadius: 16,
    paddingHorizontal: 16,
    paddingVertical: 16,
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  actionLabel: {
    color: "#ffffff",
    fontWeight: "500",
    flex: 1,
  },
  actionChevron: {
    color: "rgba(255,255,255,0.3)",
  },
});
