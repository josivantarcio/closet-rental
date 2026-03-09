import { useQuery } from "@tanstack/react-query";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { relatoriosService } from "@/lib/services/relatorios.service";

export default function RelatoriosScreen() {
  const hoje = new Date();
  const inicio = new Date(hoje.getFullYear(), hoje.getMonth() - 5, 1).toISOString().split("T")[0];
  const fim = hoje.toISOString().split("T")[0];

  const { data: faturamento } = useQuery({
    queryKey: ["relatorios", "faturamento"],
    queryFn: () => relatoriosService.faturamento(inicio, fim),
  });

  const { data: pecas } = useQuery({
    queryKey: ["relatorios", "pecas"],
    queryFn: () => relatoriosService.pecasMaisAlugadas(inicio, fim, 10),
  });

  const { data: atrasos } = useQuery({
    queryKey: ["relatorios", "atrasos"],
    queryFn: () => relatoriosService.atrasos(),
  });

  const totalReceita = faturamento?.reduce((acc, m) => acc + m.receitaBruta, 0) ?? 0;
  const totalLocacoes = faturamento?.reduce((acc, m) => acc + m.totalLocacoes, 0) ?? 0;

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.title}>Relatórios</Text>
        <Text style={styles.subtitle}>Últimos 6 meses</Text>
      </View>

      {/* KPIs gerais */}
      <View style={styles.kpiRow}>
        <Card style={styles.kpiCard}>
          <Text style={styles.kpiReceita}>R$ {totalReceita.toFixed(0)}</Text>
          <Text style={styles.kpiLabel}>Receita total</Text>
        </Card>
        <Card style={styles.kpiCard}>
          <Text style={styles.kpiLocacoes}>{totalLocacoes}</Text>
          <Text style={styles.kpiLabel}>Locações</Text>
        </Card>
        <Card style={styles.kpiCard}>
          <Text style={styles.kpiAtrasos}>{atrasos?.length ?? 0}</Text>
          <Text style={styles.kpiLabel}>Em atraso</Text>
        </Card>
      </View>

      {/* Faturamento mensal */}
      <Text style={styles.sectionTitle}>Faturamento por mês</Text>
      {faturamento?.map((m) => (
        <Card key={m.mes} style={styles.fatCard}>
          <Text style={styles.fatMes}>{m.mes}</Text>
          <View style={styles.fatRight}>
            <Text style={styles.fatReceita}>R$ {m.receitaBruta.toFixed(2)}</Text>
            <Text style={styles.fatLocacoes}>{m.totalLocacoes} locações</Text>
          </View>
        </Card>
      ))}

      {/* Peças mais alugadas */}
      <Text style={[styles.sectionTitle, styles.sectionTitleMargin]}>Top 10 peças</Text>
      {pecas?.map((p, i) => (
        <Card key={p.produtoId} style={styles.pecaCard}>
          <Text style={styles.pecaRank}>{i + 1}</Text>
          <View style={styles.pecaInfo}>
            <Text style={styles.pecaNome} numberOfLines={1}>{p.nomeProduto}</Text>
            <Text style={styles.pecaLocacoes}>{p.totalLocacoes} locações</Text>
          </View>
          <Text style={styles.pecaReceita}>R$ {p.receitaTotal.toFixed(0)}</Text>
        </Card>
      ))}

      {/* Atrasos */}
      {(atrasos?.length ?? 0) > 0 && (
        <>
          <Text style={[styles.sectionTitle, styles.sectionTitleMargin]}>Atrasos pendentes</Text>
          {atrasos?.map((a) => (
            <Card key={a.locacaoId} style={styles.atrasoCard}>
              <Text style={styles.atrasoCliente}>{a.clienteNome}</Text>
              <Text style={styles.atrasoInfo}>Locação #{a.locacaoId} · {a.diasAtraso} dias</Text>
              <Text style={styles.atrasoMulta}>Multa: R$ {a.multaEstimada.toFixed(2)}</Text>
            </Card>
          ))}
        </>
      )}
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
    paddingTop: 40,
    paddingBottom: 8,
  },
  title: {
    color: "#ffffff",
    fontSize: 24,
    fontWeight: "700",
  },
  subtitle: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 14,
  },
  kpiRow: {
    flexDirection: "row",
    gap: 12,
  },
  kpiCard: {
    flex: 1,
    alignItems: "center",
    gap: 4,
  },
  kpiReceita: {
    color: "#4ade80",
    fontSize: 20,
    fontWeight: "700",
  },
  kpiLocacoes: {
    color: "#60a5fa",
    fontSize: 20,
    fontWeight: "700",
  },
  kpiAtrasos: {
    color: "#f87171",
    fontSize: 20,
    fontWeight: "700",
  },
  kpiLabel: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
    textAlign: "center",
  },
  sectionTitle: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 18,
  },
  sectionTitleMargin: {
    marginTop: 8,
  },
  fatCard: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  fatMes: {
    color: "rgba(255,255,255,0.7)",
  },
  fatRight: {
    alignItems: "flex-end",
  },
  fatReceita: {
    color: "#ffffff",
    fontWeight: "600",
  },
  fatLocacoes: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  pecaCard: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  pecaRank: {
    color: "#e91e8c",
    fontWeight: "700",
    fontSize: 18,
    width: 24,
  },
  pecaInfo: {
    flex: 1,
  },
  pecaNome: {
    color: "#ffffff",
    fontWeight: "500",
  },
  pecaLocacoes: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  pecaReceita: {
    color: "#4ade80",
    fontWeight: "600",
  },
  atrasoCard: {
    borderColor: "rgba(239,68,68,0.3)",
    gap: 4,
  },
  atrasoCliente: {
    color: "#ffffff",
    fontWeight: "500",
  },
  atrasoInfo: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  atrasoMulta: {
    color: "#f87171",
    fontWeight: "600",
  },
});
