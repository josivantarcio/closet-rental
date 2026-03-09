import { zodResolver } from "@hookform/resolvers/zod";
import { router } from "expo-router";
import { Controller, useForm } from "react-hook-form";
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text, View } from "react-native";
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
      style={styles.flex}
    >
      <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled">
        <View style={styles.inner}>
          {/* Logo */}
          <View style={styles.logoSection}>
            <View style={styles.logoBox}>
              <Text style={styles.logoEmoji}>👗</Text>
            </View>
            <Text style={styles.logoTitle}>Atelier</Text>
            <Text style={styles.logoSubtitle}>Gestão de Aluguel de Moda</Text>
          </View>

          {/* Form */}
          <View style={styles.formSection}>
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

            <View style={styles.fieldGap} />

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

            <View style={styles.fieldGap} />

            <Button
              title="Entrar"
              onPress={handleSubmit(onSubmit)}
              loading={isSubmitting}
              size="lg"
            />
          </View>

          {/* Hint */}
          <View style={styles.hint}>
            <Text style={styles.hintTitle}>Contas de teste</Text>
            {[
              ["admin", "admin123", "Admin"],
              ["atendente", "atendente123", "Atendente"],
              ["cliente", "cliente123", "Cliente"],
            ].map(([u, p, r]) => (
              <Text key={u} style={styles.hintRow}>
                <Text style={styles.hintRole}>{r}:</Text>
                {` ${u} / ${p}`}
              </Text>
            ))}
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  flex: {
    flex: 1,
    backgroundColor: "#1a1a1a",
  },
  scrollContent: {
    flexGrow: 1,
  },
  inner: {
    flex: 1,
    paddingHorizontal: 24,
    justifyContent: "center",
    gap: 32,
  },
  logoSection: {
    alignItems: "center",
    gap: 12,
  },
  logoBox: {
    width: 80,
    height: 80,
    backgroundColor: "#e91e8c",
    borderRadius: 24,
    alignItems: "center",
    justifyContent: "center",
  },
  logoEmoji: {
    fontSize: 36,
  },
  logoTitle: {
    color: "#ffffff",
    fontSize: 30,
    fontWeight: "700",
  },
  logoSubtitle: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 16,
  },
  formSection: {
    gap: 0,
  },
  fieldGap: {
    height: 16,
  },
  hint: {
    backgroundColor: "#2a2a2a",
    borderRadius: 12,
    padding: 16,
    gap: 4,
  },
  hintTitle: {
    color: "rgba(255,255,255,0.5)",
    fontSize: 12,
    fontWeight: "600",
    textTransform: "uppercase",
    letterSpacing: 1,
    marginBottom: 4,
  },
  hintRow: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 12,
  },
  hintRole: {
    color: "rgba(255,255,255,0.6)",
  },
});
