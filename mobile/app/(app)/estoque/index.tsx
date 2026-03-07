import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { FlatList, Text, View } from "react-native";
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
  SAIDA: "text-yellow-400",
  ENTRADA: "text-green-400",
  MANUTENCAO: "text-blue-400",
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
    <View className="flex-1 bg-surface">
      <View className="pt-14 px-4 pb-3">
        <Text className="text-white text-2xl font-bold">Estoque</Text>
        {data && <Text className="text-white/40 text-sm">{data.totalElements} movimentações</Text>}
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => (
          <Card className="gap-2">
            <View className="flex-row items-center gap-3">
              <Text style={{ fontSize: 24 }}>{tipoIcon[item.tipo] ?? "📦"}</Text>
              <View className="flex-1">
                <Text className="text-white font-semibold" numberOfLines={1}>{item.nomeProduto}</Text>
                <Text className={`text-sm font-medium ${tipoColor[item.tipo]}`}>{item.tipo}</Text>
              </View>
              <Text className="text-white/60 font-bold">×{item.quantidade}</Text>
            </View>
            {item.motivo && <Text className="text-white/40 text-xs">{item.motivo}</Text>}
            <Text className="text-white/30 text-xs">{new Date(item.criadoEm).toLocaleDateString("pt-BR")}</Text>
          </Card>
        )}
        contentContainerStyle={{ padding: 16, gap: 12 }}
        ListEmptyComponent={
          isLoading
            ? <Text className="text-white/40 text-center mt-10">Carregando...</Text>
            : <Text className="text-white/40 text-center mt-10">Nenhuma movimentação encontrada.</Text>
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
