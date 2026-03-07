import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { FlatList, Image, Text, TouchableOpacity, View, Modal, ScrollView } from "react-native";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { catalogoService } from "@/lib/services/catalogo.service";
import type { ProdutoResponse } from "@/types";

function ProdutoModal({ produto, visible, onClose }: { produto: ProdutoResponse | null; visible: boolean; onClose: () => void }) {
  if (!produto) return null;
  return (
    <Modal visible={visible} animationType="slide" presentationStyle="pageSheet" onRequestClose={onClose}>
      <ScrollView className="flex-1 bg-surface">
        {produto.imagemUrl ? (
          <Image source={{ uri: produto.imagemUrl }} className="w-full h-72" resizeMode="cover" />
        ) : (
          <View className="w-full h-72 bg-surface-elevated items-center justify-center">
            <Text style={{ fontSize: 80 }}>👗</Text>
          </View>
        )}
        <View className="p-6 gap-4">
          <View>
            <Text className="text-white text-2xl font-bold">{produto.nome}</Text>
            <Text className="text-white/50 text-base">{produto.categoria}</Text>
          </View>
          <View className="flex-row gap-6">
            {produto.tamanho && (
              <View><Text className="text-white/40 text-xs">Tamanho</Text><Text className="text-white font-semibold">{produto.tamanho}</Text></View>
            )}
            {produto.cor && (
              <View><Text className="text-white/40 text-xs">Cor</Text><Text className="text-white font-semibold">{produto.cor}</Text></View>
            )}
            <View><Text className="text-white/40 text-xs">Código</Text><Text className="text-white font-semibold">{produto.codigo}</Text></View>
          </View>
          {produto.descricao && (
            <View>
              <Text className="text-white/40 text-xs mb-1">Descrição</Text>
              <Text className="text-white/80">{produto.descricao}</Text>
            </View>
          )}
          <View className="bg-surface-card border border-border rounded-2xl p-4 flex-row justify-between items-center">
            <Text className="text-white/60">Diária</Text>
            <Text className="text-primary text-2xl font-bold">R$ {produto.valorDiaria.toFixed(2)}</Text>
          </View>
          <View className={`p-3 rounded-xl ${produto.disponivel ? "bg-green-500/10 border border-green-500/30" : "bg-red-500/10 border border-red-500/30"}`}>
            <Text className={`text-center font-bold ${produto.disponivel ? "text-green-400" : "text-red-400"}`}>
              {produto.disponivel ? "✓ Disponível para locação" : "✗ Indisponível no momento"}
            </Text>
          </View>
          <Text className="text-white/30 text-xs text-center">
            Fale com um atendente para realizar sua reserva
          </Text>
          <Button title="Fechar" variant="outline" onPress={onClose} />
        </View>
      </ScrollView>
    </Modal>
  );
}

function ProdutoCard({ produto, onPress }: { produto: ProdutoResponse; onPress: () => void }) {
  return (
    <TouchableOpacity onPress={onPress} className="flex-1 m-2" activeOpacity={0.8}>
      <View className={`bg-surface-card border rounded-3xl overflow-hidden ${produto.disponivel ? "border-border" : "border-red-500/20 opacity-60"}`}>
        {produto.imagemUrl ? (
          <Image source={{ uri: produto.imagemUrl }} className="w-full h-48" resizeMode="cover" />
        ) : (
          <View className="w-full h-48 bg-surface-elevated items-center justify-center">
            <Text style={{ fontSize: 50 }}>👗</Text>
          </View>
        )}
        <View className="p-4 gap-1">
          <Text className="text-white font-bold text-base" numberOfLines={2}>{produto.nome}</Text>
          <Text className="text-white/50 text-sm">{produto.categoria}</Text>
          {produto.tamanho && <Text className="text-white/40 text-xs">Tam: {produto.tamanho}</Text>}
          <View className="flex-row justify-between items-center mt-2">
            <Text className="text-primary font-bold text-base">R$ {produto.valorDiaria.toFixed(2)}/dia</Text>
            <View className={`px-2 py-0.5 rounded-full ${produto.disponivel ? "bg-green-500/20" : "bg-red-500/20"}`}>
              <Text className={`text-xs font-bold ${produto.disponivel ? "text-green-400" : "text-red-400"}`}>
                {produto.disponivel ? "Disponível" : "Indisponível"}
              </Text>
            </View>
          </View>
        </View>
      </View>
    </TouchableOpacity>
  );
}

export default function ClienteCatalogoScreen() {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<ProdutoResponse | null>(null);

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ["catalogo-cliente", page, search],
    queryFn: () => catalogoService.listar(page, 20, search || undefined),
  });

  return (
    <View className="flex-1 bg-surface">
      {/* Header grande para tablet */}
      <View className="pt-14 px-6 pb-4 gap-4">
        <View className="flex-row items-center gap-3">
          <Text style={{ fontSize: 32 }}>👗</Text>
          <View>
            <Text className="text-white text-3xl font-bold">Nossas Peças</Text>
            <Text className="text-white/40">{data?.totalElements ?? 0} peças no catálogo</Text>
          </View>
        </View>
        <Input
          placeholder="Buscar peça por nome..."
          value={search}
          onChangeText={(v) => { setSearch(v); setPage(0); }}
          className="text-lg py-4"
        />
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        numColumns={2}
        renderItem={({ item }) => <ProdutoCard produto={item} onPress={() => setSelected(item)} />}
        contentContainerStyle={{ paddingHorizontal: 8, paddingBottom: 20 }}
        ListEmptyComponent={
          isLoading
            ? <Text className="text-white/40 text-center mt-16 text-lg">Carregando catálogo...</Text>
            : <Text className="text-white/40 text-center mt-16 text-lg">Nenhuma peça encontrada.</Text>
        }
        ListFooterComponent={
          data && !data.last ? (
            <View className="px-4 mt-2">
              <Button title="Ver mais" variant="outline" size="lg" onPress={() => setPage((p) => p + 1)} loading={isFetching} />
            </View>
          ) : null
        }
      />

      <ProdutoModal produto={selected} visible={!!selected} onClose={() => setSelected(null)} />
    </View>
  );
}
