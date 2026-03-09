import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { FlatList, StyleSheet, Text, View } from "react-native";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { api } from "@/lib/api";
import type { MovimentacaoResponse, Page } from "@/types";

const tipoIcon: Record<string, string> = {
  SAIDA: "📤",
  ENTRADA: "📥",
  MANUTENCAO: "🔧",
};

const tipoColor: Record<string, string> = {
  SAIDA: "#facc15",
  ENTRADA: "#4ade80",
  MANUTENCAO: "#60a5fa",
};

export default function EstoqueScreen() {
  const [page, setPage] = useState(0);

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ["estoque", page],
    queryFn: async (): Promise<Page<MovimentacaoResponse>> => {
      const res = await api.get("/v1/estoque/historico", { params: { page, size: 20 } });
      return res.data;
    },
  });

  return (
    <View style={styles.container}>
      <View style={styles.headerSection}>
        <Text style={styles.title}>Estoque</Text>
        {data && <Text style={styles.subtitleText}>{data.totalElements} movimentações</Text>}
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => (
          <Card style={styles.card}>
            <View style={styles.cardRow}>
              <Text style={{ fontSize: 24 }}>{tipoIcon[item.tipo] ?? "📦"}</Text>
              <View style={styles.cardInfo}>
                <Text style={styles.produtoNome} numberOfLines={1}>{item.nomeProduto}</Text>
                <Text style={[styles.tipoText, { color: tipoColor[item.tipo] ?? "#ffffff" }]}>{item.tipo}</Text>
              </View>
              <Text style={styles.quantidade}>×{item.quantidade}</Text>
            </View>
            {item.motivo && <Text style={styles.motivo}>{item.motivo}</Text>}
            <Text style={styles.data}>{new Date(item.criadoEm).toLocaleDateString("pt-BR")}</Text>
          </Card>
        )}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          isLoading
            ? <Text style={styles.emptyText}>Carregando...</Text>
            : <Text style={styles.emptyText}>Nenhuma movimentação encontrada.</Text>
        }
        ListFooterComponent={
          data && !data.last ? (
            <Button title="Carregar mais" variant="outline" onPress={() => setPage((p) => p + 1)} loading={isFetching} />
          ) : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#1a1a1a",
  },
  headerSection: {
    paddingTop: 56,
    paddingHorizontal: 16,
    paddingBottom: 12,
  },
  title: {
    color: "#ffffff",
    fontSize: 24,
    fontWeight: "700",
  },
  subtitleText: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 14,
  },
  listContent: {
    padding: 16,
    gap: 12,
  },
  card: {
    gap: 8,
  },
  cardRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  cardInfo: {
    flex: 1,
  },
  produtoNome: {
    color: "#ffffff",
    fontWeight: "600",
  },
  tipoText: {
    fontSize: 14,
    fontWeight: "500",
  },
  quantidade: {
    color: "rgba(255,255,255,0.6)",
    fontWeight: "700",
  },
  motivo: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  data: {
    color: "rgba(255,255,255,0.3)",
    fontSize: 12,
  },
  emptyText: {
    color: "rgba(255,255,255,0.4)",
    textAlign: "center",
    marginTop: 40,
  },
});
