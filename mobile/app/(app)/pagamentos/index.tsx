import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Alert, FlatList, StyleSheet, Text, View } from "react-native";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { pagamentosService } from "@/lib/services/pagamentos.service";
import type { PagamentoResponse } from "@/types";

const metodoLabel: Record<string, string> = {
  DINHEIRO: "Dinheiro",
  CARTAO_CREDITO: "Crédito",
  CARTAO_DEBITO: "Débito",
  PIX: "PIX",
  TRANSFERENCIA: "Transferência",
};

function PagamentoCard({ pag, onEstornar }: { pag: PagamentoResponse; onEstornar: (id: number) => void }) {
  return (
    <Card style={styles.card}>
      <View style={styles.cardHeader}>
        <View>
          <Text style={styles.locacaoLabel}>Locação #{pag.locacaoId}</Text>
          <Text style={styles.metodoLabel}>{metodoLabel[pag.metodo] ?? pag.metodo}</Text>
        </View>
        <View style={styles.cardRight}>
          <Text style={styles.valorText}>R$ {pag.valor.toFixed(2)}</Text>
          <Badge status={pag.status} type="pagamento" />
        </View>
      </View>
      {pag.status === "PAGO" && (
        <Button title="Estornar" size="sm" variant="danger" onPress={() => onEstornar(pag.id)} />
      )}
    </Card>
  );
}

export default function PagamentosScreen() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ["pagamentos", page],
    queryFn: () => pagamentosService.listar(page),
  });

  const estornarMutation = useMutation({
    mutationFn: pagamentosService.estornar,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["pagamentos"] }),
    onError: () => Alert.alert("Erro", "Não foi possível estornar o pagamento."),
  });

  const handleEstornar = (id: number) => {
    Alert.alert("Confirmar", "Deseja estornar este pagamento?", [
      { text: "Não", style: "cancel" },
      { text: "Sim", style: "destructive", onPress: () => estornarMutation.mutate(id) },
    ]);
  };

  const totalPago = data?.content.filter((p) => p.status === "PAGO").reduce((acc, p) => acc + p.valor, 0) ?? 0;

  return (
    <View style={styles.container}>
      <View style={styles.headerSection}>
        <Text style={styles.title}>Pagamentos</Text>
        <Card style={styles.totalCard}>
          <Text style={styles.totalLabel}>Total recebido (página)</Text>
          <Text style={styles.totalValue}>R$ {totalPago.toFixed(2)}</Text>
        </Card>
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => <PagamentoCard pag={item} onEstornar={handleEstornar} />}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          isLoading
            ? <Text style={styles.emptyText}>Carregando...</Text>
            : <Text style={styles.emptyText}>Nenhum pagamento encontrado.</Text>
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
    gap: 12,
  },
  title: {
    color: "#ffffff",
    fontSize: 24,
    fontWeight: "700",
  },
  totalCard: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  totalLabel: {
    color: "rgba(255,255,255,0.5)",
  },
  totalValue: {
    color: "#4ade80",
    fontWeight: "700",
  },
  listContent: {
    padding: 16,
    gap: 12,
  },
  card: {
    gap: 12,
  },
  cardHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  locacaoLabel: {
    color: "#ffffff",
    fontWeight: "600",
  },
  metodoLabel: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  cardRight: {
    alignItems: "flex-end",
    gap: 4,
  },
  valorText: {
    color: "#ffffff",
    fontWeight: "700",
    fontSize: 18,
  },
  emptyText: {
    color: "rgba(255,255,255,0.4)",
    textAlign: "center",
    marginTop: 40,
  },
});
