import { Text, TextInput, TextInputProps, View } from "react-native";

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
}

export function Input({ label, error, ...props }: InputProps) {
  return (
    <View className="gap-1.5">
      {label && <Text className="text-white/70 text-sm font-medium">{label}</Text>}
      <TextInput
        {...props}
        className={`bg-surface-card border rounded-xl px-4 py-3 text-white text-base ${
          error ? "border-red-500" : "border-border"
        } ${props.className ?? ""}`}
        placeholderTextColor="#666"
      />
      {error && <Text className="text-red-400 text-xs">{error}</Text>}
    </View>
  );
}
