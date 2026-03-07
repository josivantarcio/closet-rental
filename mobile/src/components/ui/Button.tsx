import { ActivityIndicator, Pressable, Text } from "react-native";

interface ButtonProps {
  title: string;
  onPress: () => void;
  loading?: boolean;
  disabled?: boolean;
  variant?: "primary" | "outline" | "ghost" | "danger";
  size?: "sm" | "md" | "lg";
}

const variantStyles = {
  primary: { container: "bg-primary active:bg-primary-dark", text: "text-white font-semibold" },
  outline: { container: "border border-primary active:bg-primary/10", text: "text-primary font-semibold" },
  ghost: { container: "active:bg-white/10", text: "text-white/70 font-medium" },
  danger: { container: "bg-red-600 active:bg-red-700", text: "text-white font-semibold" },
};

const sizeStyles = {
  sm: { container: "px-3 py-1.5 rounded-lg", text: "text-sm" },
  md: { container: "px-4 py-3 rounded-xl", text: "text-base" },
  lg: { container: "px-6 py-4 rounded-xl", text: "text-lg" },
};

export function Button({ title, onPress, loading, disabled, variant = "primary", size = "md" }: ButtonProps) {
  const v = variantStyles[variant];
  const s = sizeStyles[size];
  const isDisabled = disabled || loading;

  return (
    <Pressable
      onPress={onPress}
      disabled={isDisabled}
      className={`${v.container} ${s.container} items-center justify-center flex-row gap-2 ${isDisabled ? "opacity-50" : ""}`}
    >
      {loading && <ActivityIndicator size="small" color="white" />}
      <Text className={`${v.text} ${s.text}`}>{title}</Text>
    </Pressable>
  );
}
