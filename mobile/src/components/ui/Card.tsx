import { StyleSheet, View, ViewProps } from "react-native";

interface CardProps extends ViewProps {
  elevated?: boolean;
}

export function Card({ elevated, style, children, ...props }: CardProps) {
  return (
    <View
      style={[styles.base, elevated ? styles.elevated : styles.normal, style]}
      {...props}
    >
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  base: {
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: "#3a3a3a",
  },
  normal: {
    backgroundColor: "#2a2a2a",
  },
  elevated: {
    backgroundColor: "#242424",
  },
});
