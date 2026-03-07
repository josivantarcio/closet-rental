import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Alert, FlatList, ScrollView, Text, TouchableOpacity, View } from "react-native";
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
    <Card className="gap-3">
      <View className="flex-row justify-between items-start">
        <View className="flex-1 mr-2">
          <Text className="text-white font-semibold text-base" numberOfLines={1}>{nomePecas}</Text>
          <Text className="text-white/50 text-sm">{loc.clienteNome ?? `Cliente #${loc.clienteId}`}</Text>
        </View>
        <Badge status={loc.status} type="locacao" />
      </View>

      <View className="flex-row gap-4">
        <View>
          <Text className="text-white/40 text-xs">Período</Text>
          <Text className="text-white/80 text-sm">{loc.dataInicio} → {loc.dataFim}</Text>
        </View>
        <View>
          <Text className="text-white/40 text-xs">Total</Text>
          <Text className="text-white/80 text-sm font-semibold">R$ {loc.valorTotal.toFixed(2)}</Text>
        </View>
      </View>

      {loc.atrasada && (
        <View className="bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">
          <Text className="text-red-400 text-xs font-semibold">⚠ Locação em atraso</Text>
        </View>
      )}

      {/* Ações contextuais */}
      <View className="flex-row gap-2">
        {loc.status === "RESERVADO" && (
          <>
            <View className="flex-1">
              <Button title="Retirar" size="sm" onPress={() => onAction(loc.id, "retirar")} />
            </View>
            <View className="flex-1">
              <Button title="Cancelar" size="sm" variant="danger" onPress={() => onAction(loc.id, "cancelar")} />
            </View>
          </>
        )}
        {loc.status === "RETIRADO" && (
          <View className="flex-1">
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
    <View className="flex-1 bg-surface">
      <View className="pt-14 px-4 pb-3">
        <Text className="text-white text-2xl font-bold">Locações</Text>
      </View>

      {/* Tabs */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} className="px-4 mb-3" contentContainerStyle={{ gap: 8 }}>
        {tabs.map((t) => (
          <TouchableOpacity
            key={t.label}
            onPress={() => { setTab(t.value); setPage(0); }}
            className={`px-4 py-2 rounded-full border ${tab === t.value ? "bg-primary border-primary" : "border-border"}`}
          >
            <Text className={`text-sm font-semibold ${tab === t.value ? "text-white" : "text-white/50"}`}>{t.label}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => <LocacaoCard loc={item} onAction={handleAction} />}
        contentContainerStyle={{ padding: 16, gap: 12 }}
        ListEmptyComponent={
          isLoading ? (
            <Text className="text-white/40 text-center mt-10">Carregando...</Text>
          ) : (
            <Text className="text-white/40 text-center mt-10">Nenhuma locação encontrada.</Text>
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
