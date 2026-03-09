import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { FlatList, Image, StyleSheet, Text, View } from "react-native";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { catalogoService } from "@/lib/services/catalogo.service";
import type { ProdutoResponse } from "@/types";

function ProdutoCard({ produto }: { produto: ProdutoResponse }) {
  return (
    <View style={styles.card}>
      {produto.imagemUrl ? (
        <Image source={{ uri: produto.imagemUrl }} style={styles.cardImage} resizeMode="cover" />
      ) : (
        <View style={styles.cardImagePlaceholder}>
          <Text style={{ fontSize: 40 }}>👗</Text>
        </View>
      )}
      <View style={styles.cardBody}>
        <Text style={styles.produtoNome} numberOfLines={1}>{produto.nome}</Text>
        <Text style={styles.produtoCategoria}>{produto.categoria}</Text>
        {produto.tamanho && <Text style={styles.produtoTamanho}>Tam: {produto.tamanho}</Text>}
        <Text style={styles.produtoPreco}>R$ {produto.valorDiaria.toFixed(2)}/dia</Text>
        <View style={[styles.dispBadge, produto.disponivel ? styles.dispAtivo : styles.dispInativo]}>
          <Text style={[styles.dispText, produto.disponivel ? styles.dispTextAtivo : styles.dispTextInativo]}>
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
    <View style={styles.container}>
      <View style={styles.headerSection}>
        <Text style={styles.title}>Catálogo</Text>
        <Input
          placeholder="Buscar peças..."
          value={search}
          onChangeText={(v) => { setSearch(v); setPage(0); }}
        />
        {data && <Text style={styles.countText}>{data.totalElements} peças</Text>}
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        numColumns={2}
        renderItem={({ item }) => <ProdutoCard produto={item} />}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          isLoading
            ? <Text style={styles.emptyText}>Carregando...</Text>
            : <Text style={styles.emptyText}>Nenhuma peça encontrada.</Text>
        }
        ListFooterComponent={
          data && !data.last ? (
            <View style={styles.footerWrapper}>
              <Button title="Carregar mais" variant="outline" onPress={() => setPage((p) => p + 1)} loading={isFetching} />
            </View>
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
  countText: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  listContent: {
    paddingHorizontal: 10,
    paddingBottom: 20,
  },
  card: {
    backgroundColor: "#2a2a2a",
    borderWidth: 1,
    borderColor: "#3a3a3a",
    borderRadius: 16,
    overflow: "hidden",
    flex: 1,
    margin: 6,
  },
  cardImage: {
    width: "100%",
    height: 144,
  },
  cardImagePlaceholder: {
    width: "100%",
    height: 144,
    backgroundColor: "#242424",
    alignItems: "center",
    justifyContent: "center",
  },
  cardBody: {
    padding: 12,
    gap: 4,
  },
  produtoNome: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 14,
  },
  produtoCategoria: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 12,
  },
  produtoTamanho: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  produtoPreco: {
    color: "#e91e8c",
    fontWeight: "700",
    fontSize: 14,
    marginTop: 4,
  },
  dispBadge: {
    marginTop: 4,
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 999,
    alignSelf: "flex-start",
  },
  dispAtivo: {
    backgroundColor: "rgba(34,197,94,0.2)",
  },
  dispInativo: {
    backgroundColor: "rgba(239,68,68,0.2)",
  },
  dispText: {
    fontSize: 12,
    fontWeight: "600",
  },
  dispTextAtivo: {
    color: "#4ade80",
  },
  dispTextInativo: {
    color: "#f87171",
  },
  emptyText: {
    color: "rgba(255,255,255,0.4)",
    textAlign: "center",
    marginTop: 40,
  },
  footerWrapper: {
    paddingHorizontal: 16,
    marginTop: 8,
  },
});
