import { ActivityIndicator, Pressable, StyleSheet, Text } from "react-native";

interface ButtonProps {
  title: string;
  onPress: () => void;
  loading?: boolean;
  disabled?: boolean;
  variant?: "primary" | "outline" | "ghost" | "danger";
  size?: "sm" | "md" | "lg";
}

export function Button({ title, onPress, loading, disabled, variant = "primary", size = "md" }: ButtonProps) {
  const isDisabled = disabled || loading;

  const containerStyles = [
    styles.base,
    styles[`variant_${variant}`],
    styles[`size_${size}`],
    isDisabled && styles.disabled,
  ];

  const textStyles = [
    styles[`text_${variant}`],
    styles[`textSize_${size}`],
  ];

  return (
    <Pressable
      onPress={onPress}
      disabled={isDisabled}
      style={({ pressed }) => [
        ...containerStyles,
        pressed && styles[`pressed_${variant}`],
      ]}
    >
      {loading && <ActivityIndicator size="small" color="white" style={styles.indicator} />}
      <Text style={textStyles}>{title}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "row",
  },
  indicator: {
    marginRight: 8,
  },
  disabled: {
    opacity: 0.5,
  },

  // Variant containers
  variant_primary: {
    backgroundColor: "#e91e8c",
  },
  variant_outline: {
    borderWidth: 1,
    borderColor: "#e91e8c",
  },
  variant_ghost: {},
  variant_danger: {
    backgroundColor: "#dc2626",
  },

  // Pressed states
  pressed_primary: {
    backgroundColor: "#c2185b",
  },
  pressed_outline: {
    backgroundColor: "rgba(233,30,140,0.1)",
  },
  pressed_ghost: {
    backgroundColor: "rgba(255,255,255,0.1)",
  },
  pressed_danger: {
    backgroundColor: "#b91c1c",
  },

  // Variant text
  text_primary: {
    color: "#ffffff",
    fontWeight: "600",
  },
  text_outline: {
    color: "#e91e8c",
    fontWeight: "600",
  },
  text_ghost: {
    color: "rgba(255,255,255,0.7)",
    fontWeight: "500",
  },
  text_danger: {
    color: "#ffffff",
    fontWeight: "600",
  },

  // Size containers
  size_sm: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
  },
  size_md: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 12,
  },
  size_lg: {
    paddingHorizontal: 24,
    paddingVertical: 16,
    borderRadius: 12,
  },

  // Size text
  textSize_sm: {
    fontSize: 14,
  },
  textSize_md: {
    fontSize: 16,
  },
  textSize_lg: {
    fontSize: 18,
  },
});
