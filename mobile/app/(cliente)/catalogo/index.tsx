import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { FlatList, Image, Modal, ScrollView, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { catalogoService } from "@/lib/services/catalogo.service";
import type { ProdutoResponse } from "@/types";

function ProdutoModal({ produto, visible, onClose }: { produto: ProdutoResponse | null; visible: boolean; onClose: () => void }) {
  if (!produto) return null;
  return (
    <Modal visible={visible} animationType="slide" presentationStyle="pageSheet" onRequestClose={onClose}>
      <ScrollView style={styles.modalScroll}>
        {produto.imagemUrl ? (
          <Image source={{ uri: produto.imagemUrl }} style={styles.modalImage} resizeMode="cover" />
        ) : (
          <View style={styles.modalImagePlaceholder}>
            <Text style={{ fontSize: 80 }}>👗</Text>
          </View>
        )}
        <View style={styles.modalBody}>
          <View>
            <Text style={styles.modalTitle}>{produto.nome}</Text>
            <Text style={styles.modalCategoria}>{produto.categoria}</Text>
          </View>
          <View style={styles.modalDetails}>
            {produto.tamanho && (
              <View>
                <Text style={styles.detailLabel}>Tamanho</Text>
                <Text style={styles.detailValue}>{produto.tamanho}</Text>
              </View>
            )}
            {produto.cor && (
              <View>
                <Text style={styles.detailLabel}>Cor</Text>
                <Text style={styles.detailValue}>{produto.cor}</Text>
              </View>
            )}
            <View>
              <Text style={styles.detailLabel}>Código</Text>
              <Text style={styles.detailValue}>{produto.codigo}</Text>
            </View>
          </View>
          {produto.descricao && (
            <View>
              <Text style={styles.detailLabel}>Descrição</Text>
              <Text style={styles.descricaoText}>{produto.descricao}</Text>
            </View>
          )}
          <View style={styles.priceBox}>
            <Text style={styles.priceLabel}>Diária</Text>
            <Text style={styles.priceValue}>R$ {produto.valorDiaria.toFixed(2)}</Text>
          </View>
          <View style={[styles.disponibilidadeBanner, produto.disponivel ? styles.disponivel : styles.indisponivel]}>
            <Text style={[styles.disponibilidadeText, produto.disponivel ? styles.dispTextoAtivo : styles.dispTextoInativo]}>
              {produto.disponivel ? "✓ Disponível para locação" : "✗ Indisponível no momento"}
            </Text>
          </View>
          <Text style={styles.hint}>
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
    <TouchableOpacity onPress={onPress} style={styles.cardTouch} activeOpacity={0.8}>
      <View style={[styles.card, !produto.disponivel && styles.cardUnavailable]}>
        {produto.imagemUrl ? (
          <Image source={{ uri: produto.imagemUrl }} style={styles.cardImage} resizeMode="cover" />
        ) : (
          <View style={styles.cardImagePlaceholder}>
            <Text style={{ fontSize: 50 }}>👗</Text>
          </View>
        )}
        <View style={styles.cardBody}>
          <Text style={styles.cardNome} numberOfLines={2}>{produto.nome}</Text>
          <Text style={styles.cardCategoria}>{produto.categoria}</Text>
          {produto.tamanho && <Text style={styles.cardTamanho}>Tam: {produto.tamanho}</Text>}
          <View style={styles.cardFooter}>
            <Text style={styles.cardPreco}>R$ {produto.valorDiaria.toFixed(2)}/dia</Text>
            <View style={[styles.dispBadge, produto.disponivel ? styles.dispAtivo : styles.dispInativo]}>
              <Text style={[styles.dispText, produto.disponivel ? styles.dispTextAtivo : styles.dispTextInativo]}>
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
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.headerSection}>
        <View style={styles.headerRow}>
          <Text style={{ fontSize: 32 }}>👗</Text>
          <View>
            <Text style={styles.headerTitle}>Nossas Peças</Text>
            <Text style={styles.headerCount}>{data?.totalElements ?? 0} peças no catálogo</Text>
          </View>
        </View>
        <Input
          placeholder="Buscar peça por nome..."
          value={search}
          onChangeText={(v) => { setSearch(v); setPage(0); }}
        />
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        numColumns={2}
        renderItem={({ item }) => <ProdutoCard produto={item} onPress={() => setSelected(item)} />}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          isLoading
            ? <Text style={styles.emptyText}>Carregando catálogo...</Text>
            : <Text style={styles.emptyText}>Nenhuma peça encontrada.</Text>
        }
        ListFooterComponent={
          data && !data.last ? (
            <View style={styles.footerWrapper}>
              <Button title="Ver mais" variant="outline" size="lg" onPress={() => setPage((p) => p + 1)} loading={isFetching} />
            </View>
          ) : null
        }
      />

      <ProdutoModal produto={selected} visible={!!selected} onClose={() => setSelected(null)} />
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
    paddingHorizontal: 24,
    paddingBottom: 16,
    gap: 16,
  },
  headerRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  headerTitle: {
    color: "#ffffff",
    fontSize: 30,
    fontWeight: "700",
  },
  headerCount: {
    color: "rgba(255,255,255,0.4)",
  },
  listContent: {
    paddingHorizontal: 8,
    paddingBottom: 20,
  },
  cardTouch: {
    flex: 1,
    margin: 8,
  },
  card: {
    backgroundColor: "#2a2a2a",
    borderWidth: 1,
    borderColor: "#3a3a3a",
    borderRadius: 24,
    overflow: "hidden",
  },
  cardUnavailable: {
    borderColor: "rgba(239,68,68,0.2)",
    opacity: 0.6,
  },
  cardImage: {
    width: "100%",
    height: 192,
  },
  cardImagePlaceholder: {
    width: "100%",
    height: 192,
    backgroundColor: "#242424",
    alignItems: "center",
    justifyContent: "center",
  },
  cardBody: {
    padding: 16,
    gap: 4,
  },
  cardNome: {
    color: "#ffffff",
    fontWeight: "700",
    fontSize: 16,
  },
  cardCategoria: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  cardTamanho: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  cardFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginTop: 8,
  },
  cardPreco: {
    color: "#e91e8c",
    fontWeight: "700",
    fontSize: 16,
  },
  dispBadge: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 999,
  },
  dispAtivo: {
    backgroundColor: "rgba(34,197,94,0.2)",
  },
  dispInativo: {
    backgroundColor: "rgba(239,68,68,0.2)",
  },
  dispText: {
    fontSize: 12,
    fontWeight: "700",
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
    marginTop: 64,
    fontSize: 18,
  },
  footerWrapper: {
    paddingHorizontal: 16,
    marginTop: 8,
  },
  // Modal styles
  modalScroll: {
    flex: 1,
    backgroundColor: "#1a1a1a",
  },
  modalImage: {
    width: "100%",
    height: 288,
  },
  modalImagePlaceholder: {
    width: "100%",
    height: 288,
    backgroundColor: "#242424",
    alignItems: "center",
    justifyContent: "center",
  },
  modalBody: {
    padding: 24,
    gap: 16,
  },
  modalTitle: {
    color: "#ffffff",
    fontSize: 24,
    fontWeight: "700",
  },
  modalCategoria: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 16,
  },
  modalDetails: {
    flexDirection: "row",
    gap: 24,
  },
  detailLabel: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  detailValue: {
    color: "#ffffff",
    fontWeight: "600",
  },
  descricaoText: {
    color: "rgba(255,255,255,0.8)",
  },
  priceBox: {
    backgroundColor: "#2a2a2a",
    borderWidth: 1,
    borderColor: "#3a3a3a",
    borderRadius: 16,
    padding: 16,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  priceLabel: {
    color: "rgba(255,255,255,0.6)",
  },
  priceValue: {
    color: "#e91e8c",
    fontSize: 24,
    fontWeight: "700",
  },
  disponibilidadeBanner: {
    padding: 12,
    borderRadius: 12,
    borderWidth: 1,
  },
  disponivel: {
    backgroundColor: "rgba(34,197,94,0.1)",
    borderColor: "rgba(34,197,94,0.3)",
  },
  indisponivel: {
    backgroundColor: "rgba(239,68,68,0.1)",
    borderColor: "rgba(239,68,68,0.3)",
  },
  disponibilidadeText: {
    textAlign: "center",
    fontWeight: "700",
  },
  dispTextoAtivo: {
    color: "#4ade80",
  },
  dispTextoInativo: {
    color: "#f87171",
  },
  hint: {
    color: "rgba(255,255,255,0.3)",
    fontSize: 12,
    textAlign: "center",
  },
});
