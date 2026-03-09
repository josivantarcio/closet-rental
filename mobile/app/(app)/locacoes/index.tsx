import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Alert, FlatList, ScrollView, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { locacoesService } from "@/lib/services/locacoes.service";
import type { LocacaoResponse, StatusLocacao } from "@/types";

const tabs: { label: string; value: StatusLocacao | undefined }[] = [
  { label: "Todas", value: undefined },
  { label: "Reservadas", value: "RESERVADO" },
  { label: "Retiradas", value: "RETIRADO" },
  { label: "Devolvidas", value: "DEVOLVIDO" },
];

function LocacaoCard({ loc, onAction }: { loc: LocacaoResponse; onAction: (id: number, action: string) => void }) {
  const nomePecas = loc.itens?.map((i) => i.nomeProduto).join(", ") ?? `#${loc.id}`;

  return (
    <Card style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.cardTitleSection}>
          <Text style={styles.cardTitle} numberOfLines={1}>{nomePecas}</Text>
          <Text style={styles.cardCliente}>{loc.clienteNome ?? `Cliente #${loc.clienteId}`}</Text>
        </View>
        <Badge status={loc.status} type="locacao" />
      </View>

      <View style={styles.cardInfoRow}>
        <View>
          <Text style={styles.infoLabel}>Período</Text>
          <Text style={styles.infoValue}>{loc.dataInicio} → {loc.dataFim}</Text>
        </View>
        <View>
          <Text style={styles.infoLabel}>Total</Text>
          <Text style={styles.infoValueBold}>R$ {loc.valorTotal.toFixed(2)}</Text>
        </View>
      </View>

      {loc.atrasada && (
        <View style={styles.atrasadaBanner}>
          <Text style={styles.atrasadaText}>⚠ Locação em atraso</Text>
        </View>
      )}

      {/* Ações contextuais */}
      <View style={styles.actionsRow}>
        {loc.status === "RESERVADO" && (
          <>
            <View style={styles.actionFlex}>
              <Button title="Retirar" size="sm" onPress={() => onAction(loc.id, "retirar")} />
            </View>
            <View style={styles.actionFlex}>
              <Button title="Cancelar" size="sm" variant="danger" onPress={() => onAction(loc.id, "cancelar")} />
            </View>
          </>
        )}
        {loc.status === "RETIRADO" && (
          <View style={styles.actionFlex}>
            <Button title="Registrar Devolução" size="sm" onPress={() => onAction(loc.id, "devolver")} />
          </View>
        )}
      </View>
    </Card>
  );
}

export default function LocacoesScreen() {
  const [tab, setTab] = useState<StatusLocacao | undefined>(undefined);
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ["locacoes", tab, page],
    queryFn: () => locacoesService.listar(page, tab),
  });

  const mutation = useMutation({
    mutationFn: ({ id, action }: { id: number; action: string }) => {
      if (action === "retirar") return locacoesService.registrarRetirada(id);
      if (action === "devolver") return locacoesService.registrarDevolucao(id);
      return locacoesService.cancelar(id);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["locacoes"] }),
    onError: () => Alert.alert("Erro", "Não foi possível realizar a ação."),
  });

  const handleAction = (id: number, action: string) => {
    const labels: Record<string, string> = { retirar: "registrar a retirada", devolver: "registrar a devolução", cancelar: "cancelar esta locação" };
    Alert.alert("Confirmar", `Deseja ${labels[action]}?`, [
      { text: "Não", style: "cancel" },
      { text: "Sim", onPress: () => mutation.mutate({ id, action }) },
    ]);
  };

  return (
    <View style={styles.container}>
      <View style={styles.headerSection}>
        <Text style={styles.title}>Locações</Text>
      </View>

      {/* Tabs */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.tabsScroll} contentContainerStyle={styles.tabsContent}>
        {tabs.map((t) => (
          <TouchableOpacity
            key={t.label}
            onPress={() => { setTab(t.value); setPage(0); }}
            style={[styles.tab, tab === t.value ? styles.tabActive : styles.tabInactive]}
          >
            <Text style={[styles.tabText, tab === t.value ? styles.tabTextActive : styles.tabTextInactive]}>{t.label}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => <LocacaoCard loc={item} onAction={handleAction} />}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          isLoading ? (
            <Text style={styles.emptyText}>Carregando...</Text>
          ) : (
            <Text style={styles.emptyText}>Nenhuma locação encontrada.</Text>
          )
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
  tabsScroll: {
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  tabsContent: {
    gap: 8,
    paddingHorizontal: 16,
  },
  tab: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 999,
    borderWidth: 1,
  },
  tabActive: {
    backgroundColor: "#e91e8c",
    borderColor: "#e91e8c",
  },
  tabInactive: {
    borderColor: "#3a3a3a",
  },
  tabText: {
    fontSize: 14,
    fontWeight: "600",
  },
  tabTextActive: {
    color: "#ffffff",
  },
  tabTextInactive: {
    color: "rgba(255,255,255,0.5)",
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
    alignItems: "flex-start",
  },
  cardTitleSection: {
    flex: 1,
    marginRight: 8,
  },
  cardTitle: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 16,
  },
  cardCliente: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  cardInfoRow: {
    flexDirection: "row",
    gap: 16,
  },
  infoLabel: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  infoValue: {
    color: "rgba(255,255,255,0.8)",
    fontSize: 14,
  },
  infoValueBold: {
    color: "rgba(255,255,255,0.8)",
    fontSize: 14,
    fontWeight: "600",
  },
  atrasadaBanner: {
    backgroundColor: "rgba(239,68,68,0.1)",
    borderWidth: 1,
    borderColor: "rgba(239,68,68,0.3)",
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  atrasadaText: {
    color: "#f87171",
    fontSize: 12,
    fontWeight: "600",
  },
  actionsRow: {
    flexDirection: "row",
    gap: 8,
  },
  actionFlex: {
    flex: 1,
  },
  emptyText: {
    color: "rgba(255,255,255,0.4)",
    textAlign: "center",
    marginTop: 40,
  },
});
