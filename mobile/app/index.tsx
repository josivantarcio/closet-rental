import { Redirect } from "expo-router";
import { useAuthStore } from "@/store/auth.store";

export default function Index() {
  const { isAuthenticated, isCliente } = useAuthStore();

  if (!isAuthenticated) return <Redirect href="/(auth)/login" />;
  if (isCliente()) return <Redirect href="/(cliente)/catalogo" />;
  return <Redirect href="/(app)/dashboard" />;
}
