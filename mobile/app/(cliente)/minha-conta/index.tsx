import { useQuery } from "@tanstack/react-query";
import { Alert, ScrollView, StyleSheet, Text, View } from "react-native";
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
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.title}>Minha Conta</Text>
        <Text style={styles.subtitle}>@{username}</Text>
      </View>

      {/* Perfil */}
      {perfil && (
        <Card style={styles.perfilCard}>
          <Text style={styles.perfilNome}>{perfil.nome}</Text>
          <Text style={styles.perfilEmail}>{perfil.email}</Text>
          <Text style={styles.perfilMembro}>Membro desde {new Date(perfil.createdAt).toLocaleDateString("pt-BR")}</Text>
        </Card>
      )}

      {/* Minhas locações resumo */}
      <View>
        <Text style={styles.sectionTitle}>Minhas Locações</Text>
        {locacoes?.content.length === 0 && (
          <Text style={styles.emptyText}>Nenhuma locação encontrada.</Text>
        )}
        {locacoes?.content.slice(0, 5).map((loc) => (
          <Card key={loc.id} style={styles.locacaoCard}>
            <View style={styles.locacaoHeader}>
              <Text style={styles.locacaoNome} numberOfLines={1}>
                {loc.itens?.map((i) => i.nomeProduto).join(", ") ?? `Locação #${loc.id}`}
              </Text>
              <Badge status={loc.status} type="locacao" />
            </View>
            <Text style={styles.locacaoPeriodo}>{loc.dataInicio} → {loc.dataFim}</Text>
            <Text style={styles.locacaoValor}>R$ {loc.valorTotal.toFixed(2)}</Text>
          </Card>
        ))}
      </View>

      {/* Meus pagamentos resumo */}
      {(pagamentos?.totalElements ?? 0) > 0 && (
        <View>
          <Text style={styles.sectionTitle}>Meus Pagamentos</Text>
          {pagamentos?.content.slice(0, 3).map((pag) => (
            <Card key={pag.id} style={styles.pagamentoCard}>
              <View>
                <Text style={styles.pagamentoLocacao}>Locação #{pag.locacaoId}</Text>
                <Text style={styles.pagamentoMetodo}>{pag.metodo}</Text>
              </View>
              <View style={styles.pagamentoRight}>
                <Text style={styles.pagamentoValor}>R$ {pag.valor.toFixed(2)}</Text>
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

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
    backgroundColor: "#1a1a1a",
  },
  content: {
    padding: 20,
    gap: 20,
  },
  header: {
    paddingTop: 40,
    paddingBottom: 8,
  },
  title: {
    color: "#ffffff",
    fontSize: 30,
    fontWeight: "700",
  },
  subtitle: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 16,
  },
  perfilCard: {
    gap: 12,
  },
  perfilNome: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 18,
  },
  perfilEmail: {
    color: "rgba(255,255,255,0.5)",
  },
  perfilMembro: {
    color: "rgba(255,255,255,0.3)",
    fontSize: 12,
  },
  sectionTitle: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 18,
    marginBottom: 12,
  },
  emptyText: {
    color: "rgba(255,255,255,0.4)",
  },
  locacaoCard: {
    marginBottom: 12,
    gap: 8,
  },
  locacaoHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  locacaoNome: {
    color: "#ffffff",
    fontWeight: "500",
    flex: 1,
    marginRight: 8,
  },
  locacaoPeriodo: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  locacaoValor: {
    color: "rgba(255,255,255,0.7)",
    fontWeight: "600",
  },
  pagamentoCard: {
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  pagamentoLocacao: {
    color: "#ffffff",
    fontWeight: "500",
  },
  pagamentoMetodo: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  pagamentoRight: {
    alignItems: "flex-end",
    gap: 4,
  },
  pagamentoValor: {
    color: "#ffffff",
    fontWeight: "700",
  },
});
