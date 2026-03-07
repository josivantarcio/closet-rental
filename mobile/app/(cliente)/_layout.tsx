import { Redirect, Tabs } from "expo-router";
import { Text } from "react-native";
import { useAuthStore } from "@/store/auth.store";

function TabIcon({ icon, focused }: { icon: string; focused: boolean }) {
  return <Text style={{ fontSize: 26, opacity: focused ? 1 : 0.4 }}>{icon}</Text>;
}

export default function ClienteLayout() {
  const { isAuthenticated } = useAuthStore();
  if (!isAuthenticated) return <Redirect href="/(auth)/login" />;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: { backgroundColor: "#0f0f0f", borderTopColor: "#2a2a2a", height: 70, paddingBottom: 10 },
        tabBarActiveTintColor: "#e91e8c",
        tabBarInactiveTintColor: "#555",
        tabBarLabelStyle: { fontSize: 12, fontWeight: "700" },
      }}
    >
      <Tabs.Screen
        name="catalogo/index"
        options={{ title: "Catálogo", tabBarIcon: ({ focused }) => <TabIcon icon="👗" focused={focused} /> }}
      />
      <Tabs.Screen
        name="minha-conta/index"
        options={{ title: "Minha Conta", tabBarIcon: ({ focused }) => <TabIcon icon="👤" focused={focused} /> }}
      />
    </Tabs>
  );
}
