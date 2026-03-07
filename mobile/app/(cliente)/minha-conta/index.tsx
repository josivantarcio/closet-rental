import { useQuery } from "@tanstack/react-query";
import { Alert, ScrollView, Text, TouchableOpacity, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { minhaContaService } from "@/lib/services/minhaConta.service";
import { useAuthStore } from "@/store/auth.store";
import { authService } from "@/lib/services/auth.service";
import { router } from "expo-router";

export default function MinhaContaClienteScreen() {
  const { clearAuth, username } = useAuthStore();

  const { data: perfil } = useQuery({
    queryKey: ["minha-conta", "perfil"],
    queryFn: minhaContaService.getPerfil,
  });

  const { data: locacoes } = useQuery({
    queryKey: ["minha-conta", "locacoes"],
    queryFn: () => minhaContaService.getMinhasLocacoes(0),
  });

  const { data: pagamentos } = useQuery({
    queryKey: ["minha-conta", "pagamentos"],
    queryFn: () => minhaContaService.getMeusPagamentos(0),
  });

  const handleLogout = () => {
    Alert.alert("Sair", "Deseja encerrar a sessão?", [
      { text: "Cancelar", style: "cancel" },
      {
        text: "Sair", style: "destructive", onPress: async () => {
          await authService.logout();
          await clearAuth();
          router.replace("/(auth)/login");
        },
      },
    ]);
  };

  return (
    <ScrollView className="flex-1 bg-surface" contentContainerStyle={{ padding: 20, gap: 20 }}>
      <View className="pt-10 pb-2">
        <Text className="text-white text-3xl font-bold">Minha Conta</Text>
        <Text className="text-white/50 text-base">@{username}</Text>
      </View>

      {/* Perfil */}
      {perfil && (
        <Card className="gap-3">
          <Text className="text-white font-semibold text-lg">{perfil.nome}</Text>
          <Text className="text-white/50">{perfil.email}</Text>
          <Text className="text-white/30 text-xs">Membro desde {new Date(perfil.createdAt).toLocaleDateString("pt-BR")}</Text>
        </Card>
      )}

      {/* Minhas locações resumo */}
      <View>
        <Text className="text-white font-semibold text-lg mb-3">Minhas Locações</Text>
        {locacoes?.content.length === 0 && (
          <Text className="text-white/40">Nenhuma locação encontrada.</Text>
        )}
        {locacoes?.content.slice(0, 5).map((loc) => (
          <Card key={loc.id} className="mb-3 gap-2">
            <View className="flex-row justify-between items-center">
              <Text className="text-white font-medium flex-1 mr-2" numberOfLines={1}>
                {loc.itens?.map((i) => i.nomeProduto).join(", ") ?? `Locação #${loc.id}`}
              </Text>
              <Badge status={loc.status} type="locacao" />
            </View>
            <Text className="text-white/50 text-sm">{loc.dataInicio} → {loc.dataFim}</Text>
            <Text className="text-white/70 font-semibold">R$ {loc.valorTotal.toFixed(2)}</Text>
          </Card>
        ))}
      </View>

      {/* Meus pagamentos resumo */}
      {(pagamentos?.totalElements ?? 0) > 0 && (
        <View>
          <Text className="text-white font-semibold text-lg mb-3">Meus Pagamentos</Text>
          {pagamentos?.content.slice(0, 3).map((pag) => (
            <Card key={pag.id} className="mb-3 flex-row justify-between items-center">
              <View>
                <Text className="text-white font-medium">Locação #{pag.locacaoId}</Text>
                <Text className="text-white/50 text-sm">{pag.metodo}</Text>
              </View>
              <View className="items-end gap-1">
                <Text className="text-white font-bold">R$ {pag.valor.toFixed(2)}</Text>
                <Badge status={pag.status} type="pagamento" />
              </View>
            </Card>
          ))}
        </View>
      )}

      <Button title="Sair" variant="danger" onPress={handleLogout} />
    </ScrollView>
  );
}
