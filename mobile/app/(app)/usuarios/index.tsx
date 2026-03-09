import { useQuery } from "@tanstack/react-query";
import { FlatList, StyleSheet, Text, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { api } from "@/lib/api";
import type { Page, UsuarioResponse } from "@/types";
import { useAuthStore } from "@/store/auth.store";

const roleLabel: Record<string, string> = {
  ADMIN: "Admin",
  ATENDENTE: "Atendente",
  CLIENTE: "Cliente",
  SUPER_ADMIN: "Super Admin",
};

const roleColor: Record<string, string> = {
  ADMIN: "#c084fc",
  ATENDENTE: "#60a5fa",
  CLIENTE: "#4ade80",
  SUPER_ADMIN: "#facc15",
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
      <View style={styles.restricted}>
        <Text style={{ fontSize: 48 }}>🔒</Text>
        <Text style={styles.restrictedTitle}>Acesso restrito</Text>
        <Text style={styles.restrictedSubtitle}>Apenas administradores podem gerenciar usuários.</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.headerSection}>
        <Text style={styles.title}>Usuários</Text>
        {data && <Text style={styles.subtitleText}>{data.totalElements} usuários</Text>}
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => (
          <Card style={styles.card}>
            <View style={styles.cardRow}>
              <View style={styles.cardInfo}>
                <Text style={styles.userName}>{item.nome}</Text>
                <Text style={styles.userUsername}>@{item.username}</Text>
              </View>
              <View style={styles.cardRight}>
                <Text style={[styles.roleText, { color: roleColor[item.role] ?? "#ffffff" }]}>
                  {roleLabel[item.role] ?? item.role}
                </Text>
                <View style={[styles.statusBadge, item.ativo ? styles.statusAtivo : styles.statusInativo]}>
                  <Text style={[styles.statusText, item.ativo ? styles.statusTextAtivo : styles.statusTextInativo]}>
                    {item.ativo ? "Ativo" : "Inativo"}
                  </Text>
                </View>
              </View>
            </View>
          </Card>
        )}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          isLoading
            ? <Text style={styles.emptyText}>Carregando...</Text>
            : <Text style={styles.emptyText}>Nenhum usuário encontrado.</Text>
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
  restricted: {
    flex: 1,
    backgroundColor: "#1a1a1a",
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 32,
  },
  restrictedTitle: {
    color: "#ffffff",
    fontSize: 20,
    fontWeight: "700",
    marginTop: 16,
  },
  restrictedSubtitle: {
    color: "rgba(255,255,255,0.5)",
    textAlign: "center",
    marginTop: 8,
  },
  headerSection: {
    paddingTop: 56,
    paddingHorizontal: 16,
    paddingBottom: 12,
  },
  title: {
    color: "#ffffff",
    fontSize: 24,
    fontWeight: "700",
  },
  subtitleText: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 14,
  },
  listContent: {
    padding: 16,
    gap: 12,
  },
  card: {
    gap: 8,
  },
  cardRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  cardInfo: {
    flex: 1,
  },
  userName: {
    color: "#ffffff",
    fontWeight: "600",
  },
  userUsername: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 14,
  },
  cardRight: {
    alignItems: "flex-end",
    gap: 4,
  },
  roleText: {
    fontSize: 14,
    fontWeight: "600",
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 2,
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
