import { zodResolver } from "@hookform/resolvers/zod";
import { router } from "expo-router";
import { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import * as SecureStore from "expo-secure-store";
import { z } from "zod";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { authService } from "@/lib/services/auth.service";
import { SERVER_URL_KEY, getApiUrl } from "@/lib/api";
import { useAuthStore } from "@/store/auth.store";

const schema = z.object({
  username: z.string().min(1, "Usuário obrigatório"),
  password: z.string().min(1, "Senha obrigatória"),
});

type FormData = z.infer<typeof schema>;

export default function LoginScreen() {
  const setAuth = useAuthStore((s) => s.setAuth);
  const isCliente = useAuthStore((s) => s.isCliente);

  const [showPassword, setShowPassword] = useState(false);
  const [showServerConfig, setShowServerConfig] = useState(false);
  const [serverUrl, setServerUrl] = useState("");
  const [serverSaved, setServerSaved] = useState(false);

  useEffect(() => {
    getApiUrl().then(setServerUrl);
  }, []);

  const saveServerUrl = async () => {
    const url = serverUrl.trim().replace(/\/$/, "");
    if (!url) return;
    await SecureStore.setItemAsync(SERVER_URL_KEY, url);
    setServerSaved(true);
    setTimeout(() => setServerSaved(false), 2000);
  };

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

            {/* Campo senha com toggle */}
            <Controller
              control={control}
              name="password"
              render={({ field: { onChange, value } }) => (
                <View>
                  <Text style={styles.label}>Senha</Text>
                  <View style={styles.passwordRow}>
                    <TextInput
                      style={[
                        styles.passwordInput,
                        errors.password ? styles.inputError : styles.inputNormal,
                      ]}
                      placeholder="Digite sua senha"
                      placeholderTextColor="#666"
                      value={value}
                      onChangeText={onChange}
                      secureTextEntry={!showPassword}
                      autoCapitalize="none"
                      autoCorrect={false}
                    />
                    <Pressable
                      onPress={() => setShowPassword((v) => !v)}
                      style={styles.eyeButton}
                    >
                      <Text style={styles.eyeIcon}>{showPassword ? "🙈" : "👁️"}</Text>
                    </Pressable>
                  </View>
                  {errors.password && (
                    <Text style={styles.errorText}>{errors.password.message}</Text>
                  )}
                </View>
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

          {/* Configuração do servidor */}
          <Pressable onPress={() => setShowServerConfig((v) => !v)} style={styles.serverToggle}>
            <Text style={styles.serverToggleText}>
              {showServerConfig ? "▲" : "▼"} Configurar servidor
            </Text>
          </Pressable>

          {showServerConfig && (
            <View style={styles.serverConfig}>
              <Text style={styles.serverConfigTitle}>URL do Backend</Text>
              <Text style={styles.serverConfigHint}>
                Ex: http://192.168.1.111:8080
              </Text>
              <View style={styles.serverInputRow}>
                <TextInput
                  style={styles.serverInput}
                  value={serverUrl}
                  onChangeText={setServerUrl}
                  placeholder="http://192.168.1.x:8080"
                  placeholderTextColor="#555"
                  autoCapitalize="none"
                  autoCorrect={false}
                  keyboardType="url"
                />
                <Pressable
                  onPress={saveServerUrl}
                  style={[styles.saveButton, serverSaved && styles.saveButtonSaved]}
                >
                  <Text style={styles.saveButtonText}>{serverSaved ? "✓" : "Salvar"}</Text>
                </Pressable>
              </View>
            </View>
          )}

          {/* Hint contas de teste */}
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
    gap: 24,
    paddingVertical: 40,
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
  label: {
    color: "rgba(255,255,255,0.7)",
    fontSize: 14,
    fontWeight: "500",
    marginBottom: 6,
  },
  passwordRow: {
    flexDirection: "row",
    alignItems: "center",
    position: "relative",
  },
  passwordInput: {
    flex: 1,
    backgroundColor: "#2a2a2a",
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    paddingRight: 48,
    color: "#ffffff",
    fontSize: 16,
  },
  inputNormal: {
    borderColor: "#3a3a3a",
  },
  inputError: {
    borderColor: "#ef4444",
  },
  eyeButton: {
    position: "absolute",
    right: 12,
    padding: 4,
  },
  eyeIcon: {
    fontSize: 20,
  },
  errorText: {
    color: "#f87171",
    fontSize: 12,
    marginTop: 4,
  },
  serverToggle: {
    alignItems: "center",
    paddingVertical: 4,
  },
  serverToggleText: {
    color: "rgba(255,255,255,0.3)",
    fontSize: 12,
  },
  serverConfig: {
    backgroundColor: "#2a2a2a",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#3a3a3a",
    padding: 16,
    gap: 8,
  },
  serverConfigTitle: {
    color: "rgba(255,255,255,0.7)",
    fontSize: 13,
    fontWeight: "600",
  },
  serverConfigHint: {
    color: "rgba(255,255,255,0.3)",
    fontSize: 11,
    marginTop: -4,
  },
  serverInputRow: {
    flexDirection: "row",
    gap: 8,
    marginTop: 4,
  },
  serverInput: {
    flex: 1,
    backgroundColor: "#1a1a1a",
    borderWidth: 1,
    borderColor: "#3a3a3a",
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: "#ffffff",
    fontSize: 13,
  },
  saveButton: {
    backgroundColor: "#e91e8c",
    borderRadius: 10,
    paddingHorizontal: 16,
    justifyContent: "center",
  },
  saveButtonSaved: {
    backgroundColor: "#4ade80",
  },
  saveButtonText: {
    color: "#ffffff",
    fontWeight: "700",
    fontSize: 13,
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
