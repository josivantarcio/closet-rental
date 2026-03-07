import { useQuery } from "@tanstack/react-query";
import { Alert, FlatList, Text, TouchableOpacity, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { api } from "@/lib/api";
import type { Page, UsuarioResponse } from "@/types";
import { useAuthStore } from "@/store/auth.store";
import { router } from "expo-router";

const roleLabel: Record<string, string> = {
  ADMIN: "Admin",
  ATENDENTE: "Atendente",
  CLIENTE: "Cliente",
  SUPER_ADMIN: "Super Admin",
};

const roleColor: Record<string, string> = {
  ADMIN: "text-purple-400",
  ATENDENTE: "text-blue-400",
  CLIENTE: "text-green-400",
  SUPER_ADMIN: "text-yellow-400",
};

export default function UsuariosScreen() {
  const { isAdmin } = useAuthStore();

  const { data, isLoading } = useQuery({
    queryKey: ["usuarios"],
    queryFn: async (): Promise<Page<UsuarioResponse>> => {
      const res = await api.get("/v1/usuarios");
      return res.data;
    },
    enabled: isAdmin(),
  });

  if (!isAdmin()) {
    return (
      <View className="flex-1 bg-surface items-center justify-center px-8">
        <Text style={{ fontSize: 48 }}>🔒</Text>
        <Text className="text-white text-xl font-bold mt-4">Acesso restrito</Text>
        <Text className="text-white/50 text-center mt-2">Apenas administradores podem gerenciar usuários.</Text>
      </View>
    );
  }

  return (
    <View className="flex-1 bg-surface">
      <View className="pt-14 px-4 pb-3">
        <Text className="text-white text-2xl font-bold">Usuários</Text>
        {data && <Text className="text-white/40 text-sm">{data.totalElements} usuários</Text>}
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => (
          <Card className="gap-2">
            <View className="flex-row justify-between items-center">
              <View className="flex-1">
                <Text className="text-white font-semibold">{item.nome}</Text>
                <Text className="text-white/50 text-sm">@{item.username}</Text>
              </View>
              <View className="items-end gap-1">
                <Text className={`text-sm font-semibold ${roleColor[item.role] ?? "text-white"}`}>
                  {roleLabel[item.role] ?? item.role}
                </Text>
                <View className={`px-2 py-0.5 rounded-full ${item.ativo ? "bg-green-500/20" : "bg-red-500/20"}`}>
                  <Text className={`text-xs ${item.ativo ? "text-green-400" : "text-red-400"}`}>
                    {item.ativo ? "Ativo" : "Inativo"}
                  </Text>
                </View>
              </View>
            </View>
          </Card>
        )}
        contentContainerStyle={{ padding: 16, gap: 12 }}
        ListEmptyComponent={
          isLoading
            ? <Text className="text-white/40 text-center mt-10">Carregando...</Text>
            : <Text className="text-white/40 text-center mt-10">Nenhum usuário encontrado.</Text>
        }
      />
    </View>
  );
}
