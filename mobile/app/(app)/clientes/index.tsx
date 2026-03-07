import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Alert, FlatList, Text, TouchableOpacity, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { clientesService } from "@/lib/services/clientes.service";
import type { ClienteResponse } from "@/types";

function ClienteCard({ cliente, onToggle }: { cliente: ClienteResponse; onToggle: (id: number, ativo: boolean) => void }) {
  return (
    <Card className="gap-3">
      <View className="flex-row justify-between items-start">
        <View className="flex-1">
          <Text className="text-white font-semibold text-base">{cliente.nome}</Text>
          <Text className="text-white/50 text-sm">{cliente.email}</Text>
          <Text className="text-white/40 text-xs mt-0.5">{cliente.telefone}</Text>
        </View>
        <View className={`px-2.5 py-1 rounded-full ${cliente.ativo ? "bg-green-500/20" : "bg-red-500/20"}`}>
          <Text className={`text-xs font-semibold ${cliente.ativo ? "text-green-400" : "text-red-400"}`}>
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
    <View className="flex-1 bg-surface">
      <View className="pt-14 px-4 pb-3 gap-3">
        <Text className="text-white text-2xl font-bold">Clientes</Text>
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
        contentContainerStyle={{ padding: 16, gap: 12 }}
        ListEmptyComponent={
          isLoading
            ? <Text className="text-white/40 text-center mt-10">Carregando...</Text>
            : <Text className="text-white/40 text-center mt-10">Nenhum cliente encontrado.</Text>
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
