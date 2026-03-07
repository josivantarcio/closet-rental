import { zodResolver } from "@hookform/resolvers/zod";
import { router } from "expo-router";
import { Controller, useForm } from "react-hook-form";
import { KeyboardAvoidingView, Platform, ScrollView, Text, View } from "react-native";
import { z } from "zod";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { authService } from "@/lib/services/auth.service";
import { useAuthStore } from "@/store/auth.store";

const schema = z.object({
  username: z.string().min(1, "Usuário obrigatório"),
  password: z.string().min(1, "Senha obrigatória"),
});

type FormData = z.infer<typeof schema>;

export default function LoginScreen() {
  const setAuth = useAuthStore((s) => s.setAuth);
  const isCliente = useAuthStore((s) => s.isCliente);

  const { control, handleSubmit, formState: { errors, isSubmitting }, setError } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authService.login(data);
      await setAuth(res.token, res.username, res.roles, res.tenantId);
      const cliente = res.roles.includes("ROLE_CLIENTE");
      router.replace(cliente ? "/(cliente)/catalogo" : "/(app)/dashboard");
    } catch {
      setError("password", { message: "Usuário ou senha incorretos" });
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      className="flex-1 bg-surface"
    >
      <ScrollView contentContainerStyle={{ flexGrow: 1 }} keyboardShouldPersistTaps="handled">
        <View className="flex-1 px-6 justify-center gap-8">
          {/* Logo */}
          <View className="items-center gap-3">
            <View className="w-20 h-20 bg-primary rounded-3xl items-center justify-center">
              <Text className="text-white text-4xl">👗</Text>
            </View>
            <Text className="text-white text-3xl font-bold">Atelier</Text>
            <Text className="text-white/50 text-base">Gestão de Aluguel de Moda</Text>
          </View>

          {/* Form */}
          <View className="gap-4">
            <Controller
              control={control}
              name="username"
              render={({ field: { onChange, value } }) => (
                <Input
                  label="Usuário"
                  placeholder="Digite seu usuário"
                  value={value}
                  onChangeText={onChange}
                  autoCapitalize="none"
                  autoCorrect={false}
                  error={errors.username?.message}
                />
              )}
            />

            <Controller
              control={control}
              name="password"
              render={({ field: { onChange, value } }) => (
                <Input
                  label="Senha"
                  placeholder="Digite sua senha"
                  value={value}
                  onChangeText={onChange}
                  secureTextEntry
                  error={errors.password?.message}
                />
              )}
            />

            <Button
              title="Entrar"
              onPress={handleSubmit(onSubmit)}
              loading={isSubmitting}
              size="lg"
            />
          </View>

          {/* Hint */}
          <View className="bg-surface-card rounded-xl p-4 gap-1">
            <Text className="text-white/50 text-xs font-semibold uppercase tracking-wider mb-1">Contas de teste</Text>
            {[
              ["admin", "admin123", "Admin"],
              ["atendente", "atendente123", "Atendente"],
              ["cliente", "cliente123", "Cliente"],
            ].map(([u, p, r]) => (
              <Text key={u} className="text-white/40 text-xs">
                <Text className="text-white/60">{r}:</Text> {u} / {p}
              </Text>
            ))}
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
