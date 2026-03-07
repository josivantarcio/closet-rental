import { View, ViewProps } from "react-native";

interface CardProps extends ViewProps {
  elevated?: boolean;
}

export function Card({ elevated, className, children, ...props }: CardProps) {
  return (
    <View
      className={`${elevated ? "bg-surface-elevated" : "bg-surface-card"} rounded-2xl p-4 border border-border ${className ?? ""}`}
      {...props}
    >
      {children}
    </View>
  );
}
