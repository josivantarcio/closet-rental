import { Redirect, Tabs } from "expo-router";
import { Text } from "react-native";
import { useAuthStore } from "@/store/auth.store";

function TabIcon({ icon, focused }: { icon: string; focused: boolean }) {
  return <Text style={{ fontSize: 22, opacity: focused ? 1 : 0.45 }}>{icon}</Text>;
}

export default function AppLayout() {
  const { isAuthenticated, isCliente } = useAuthStore();

  if (!isAuthenticated) return <Redirect href="/(auth)/login" />;
  if (isCliente()) return <Redirect href="/(cliente)/catalogo" />;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: { backgroundColor: "#1a1a1a", borderTopColor: "#3a3a3a", height: 60, paddingBottom: 8 },
        tabBarActiveTintColor: "#e91e8c",
        tabBarInactiveTintColor: "#666",
        tabBarLabelStyle: { fontSize: 10, fontWeight: "600" },
      }}
    >
      <Tabs.Screen
        name="dashboard/index"
        options={{ title: "Início", tabBarIcon: ({ focused }) => <TabIcon icon="🏠" focused={focused} /> }}
      />
      <Tabs.Screen
        name="locacoes/index"
        options={{ title: "Locações", tabBarIcon: ({ focused }) => <TabIcon icon="📋" focused={focused} /> }}
      />
      <Tabs.Screen
        name="clientes/index"
        options={{ title: "Clientes", tabBarIcon: ({ focused }) => <TabIcon icon="👥" focused={focused} /> }}
      />
      <Tabs.Screen
        name="catalogo/index"
        options={{ title: "Catálogo", tabBarIcon: ({ focused }) => <TabIcon icon="👗" focused={focused} /> }}
      />
      <Tabs.Screen
        name="pagamentos/index"
        options={{ title: "Pagamentos", tabBarIcon: ({ focused }) => <TabIcon icon="💳" focused={focused} /> }}
      />
      <Tabs.Screen
        name="estoque/index"
        options={{ title: "Estoque", tabBarIcon: ({ focused }) => <TabIcon icon="📦" focused={focused} /> }}
      />
      <Tabs.Screen
        name="relatorios/index"
        options={{ title: "Relatórios", tabBarIcon: ({ focused }) => <TabIcon icon="📊" focused={focused} /> }}
      />
      <Tabs.Screen
        name="usuarios/index"
        options={{ title: "Usuários", tabBarIcon: ({ focused }) => <TabIcon icon="⚙️" focused={focused} /> }}
      />
    </Tabs>
  );
}
