import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Alert, FlatList, StyleSheet, Text, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { clientesService } from "@/lib/services/clientes.service";
import type { ClienteResponse } from "@/types";

function ClienteCard({ cliente, onToggle }: { cliente: ClienteResponse; onToggle: (id: number, ativo: boolean) => void }) {
  return (
    <Card style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.cardInfo}>
          <Text style={styles.clienteNome}>{cliente.nome}</Text>
          <Text style={styles.clienteEmail}>{cliente.email}</Text>
          <Text style={styles.clienteTelefone}>{cliente.telefone}</Text>
        </View>
        <View style={[styles.statusBadge, cliente.ativo ? styles.statusAtivo : styles.statusInativo]}>
          <Text style={[styles.statusText, cliente.ativo ? styles.statusTextAtivo : styles.statusTextInativo]}>
            {cliente.ativo ? "Ativo" : "Inativo"}
          </Text>
        </View>
      </View>
      <Button
        title={cliente.ativo ? "Inativar" : "Reativar"}
        size="sm"
        variant={cliente.ativo ? "danger" : "outline"}
        onPress={() => onToggle(cliente.id, cliente.ativo)}
      />
    </Card>
  );
}

export default function ClientesScreen() {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["clientes", page, search],
    queryFn: () => clientesService.listar(page, search || undefined),
  });

  const toggleMutation = useMutation({
    mutationFn: ({ id, ativo }: { id: number; ativo: boolean }) =>
      ativo ? clientesService.inativar(id) : clientesService.reativar(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["clientes"] }),
    onError: () => Alert.alert("Erro", "Não foi possível alterar o status do cliente."),
  });

  const handleToggle = (id: number, ativo: boolean) => {
    Alert.alert("Confirmar", `Deseja ${ativo ? "inativar" : "reativar"} este cliente?`, [
      { text: "Não", style: "cancel" },
      { text: "Sim", onPress: () => toggleMutation.mutate({ id, ativo }) },
    ]);
  };

  return (
    <View style={styles.container}>
      <View style={styles.headerSection}>
        <Text style={styles.title}>Clientes</Text>
        <Input
          placeholder="Buscar por nome..."
          value={search}
          onChangeText={(v) => { setSearch(v); setPage(0); }}
        />
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => <ClienteCard cliente={item} onToggle={handleToggle} />}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          isLoading
            ? <Text style={styles.emptyText}>Carregando...</Text>
            : <Text style={styles.emptyText}>Nenhum cliente encontrado.</Text>
        }
        ListFooterComponent={
          data && !data.last ? (
            <Button title="Carregar mais" variant="outline" onPress={() => setPage((p) => p + 1)} />
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
  cardInfo: {
    flex: 1,
  },
  clienteNome: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 16,
  },
  clienteEmail: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  clienteTelefone: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
    marginTop: 2,
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
  },
  statusAtivo: {
    backgroundColor: "rgba(34,197,94,0.2)",
  },
  statusInativo: {
    backgroundColor: "rgba(239,68,68,0.2)",
  },
  statusText: {
    fontSize: 12,
    fontWeight: "600",
  },
  statusTextAtivo: {
    color: "#4ade80",
  },
  statusTextInativo: {
    color: "#f87171",
  },
  emptyText: {
    color: "rgba(255,255,255,0.4)",
    textAlign: "center",
    marginTop: 40,
  },
});
