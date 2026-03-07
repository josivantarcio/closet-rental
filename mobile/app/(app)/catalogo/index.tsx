import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { FlatList, Image, Text, TouchableOpacity, View } from "react-native";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { catalogoService } from "@/lib/services/catalogo.service";
import type { ProdutoResponse } from "@/types";

function ProdutoCard({ produto }: { produto: ProdutoResponse }) {
  return (
    <View className="bg-surface-card border border-border rounded-2xl overflow-hidden flex-1 m-1.5">
      {produto.imagemUrl ? (
        <Image source={{ uri: produto.imagemUrl }} className="w-full h-36" resizeMode="cover" />
      ) : (
        <View className="w-full h-36 bg-surface-elevated items-center justify-center">
          <Text style={{ fontSize: 40 }}>👗</Text>
        </View>
      )}
      <View className="p-3 gap-1">
        <Text className="text-white font-semibold text-sm" numberOfLines={1}>{produto.nome}</Text>
        <Text className="text-white/50 text-xs">{produto.categoria}</Text>
        {produto.tamanho && <Text className="text-white/40 text-xs">Tam: {produto.tamanho}</Text>}
        <Text className="text-primary font-bold text-sm mt-1">R$ {produto.valorDiaria.toFixed(2)}/dia</Text>
        <View className={`mt-1 px-2 py-0.5 rounded-full self-start ${produto.disponivel ? "bg-green-500/20" : "bg-red-500/20"}`}>
          <Text className={`text-xs font-semibold ${produto.disponivel ? "text-green-400" : "text-red-400"}`}>
            {produto.disponivel ? "Disponível" : "Indisponível"}
          </Text>
        </View>
      </View>
    </View>
  );
}

export default function CatalogoScreen() {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ["catalogo", page, search],
    queryFn: () => catalogoService.listar(page, 20, search || undefined),
  });

  return (
    <View className="flex-1 bg-surface">
      <View className="pt-14 px-4 pb-3 gap-3">
        <Text className="text-white text-2xl font-bold">Catálogo</Text>
        <Input
          placeholder="Buscar peças..."
          value={search}
          onChangeText={(v) => { setSearch(v); setPage(0); }}
        />
        {data && <Text className="text-white/40 text-xs">{data.totalElements} peças</Text>}
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        numColumns={2}
        renderItem={({ item }) => <ProdutoCard produto={item} />}
        contentContainerStyle={{ paddingHorizontal: 10, paddingBottom: 20 }}
        ListEmptyComponent={
          isLoading
            ? <Text className="text-white/40 text-center mt-10">Carregando...</Text>
            : <Text className="text-white/40 text-center mt-10">Nenhuma peça encontrada.</Text>
        }
        ListFooterComponent={
          data && !data.last ? (
            <View className="px-4 mt-2">
              <Button title="Carregar mais" variant="outline" onPress={() => setPage((p) => p + 1)} loading={isFetching} />
            </View>
          ) : null
        }
      />
    </View>
  );
}
