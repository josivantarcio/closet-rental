import { StyleSheet, Text, View } from "react-native";
import type { StatusLocacao, StatusPagamento } from "@/types";

const locacaoLabels: Record<StatusLocacao, string> = {
  RESERVADO: "Reservado",
  RETIRADO: "Retirado",
  DEVOLVIDO: "Devolvido",
  CANCELADO: "Cancelado",
};

const pagamentoLabels: Record<StatusPagamento, string> = {
  PENDENTE: "Pendente",
  PAGO: "Pago",
  ESTORNADO: "Estornado",
};

interface BadgeProps {
  status: StatusLocacao | StatusPagamento;
  type?: "locacao" | "pagamento";
}

export function Badge({ status, type = "locacao" }: BadgeProps) {
  const label = type === "locacao"
    ? locacaoLabels[status as StatusLocacao]
    : pagamentoLabels[status as StatusPagamento];

  const containerStyle = [styles.container, statusContainerStyle(status)];
  const textStyle = [styles.text, statusTextStyle(status)];

  return (
    <View style={containerStyle}>
      <Text style={textStyle}>{label}</Text>
    </View>
  );
}

function statusContainerStyle(status: StatusLocacao | StatusPagamento) {
  switch (status) {
    case "RESERVADO": return styles.bgBlue;
    case "RETIRADO": return styles.bgYellow;
    case "DEVOLVIDO": return styles.bgGreen;
    case "CANCELADO": return styles.bgRed;
    case "PENDENTE": return styles.bgYellow;
    case "PAGO": return styles.bgGreen;
    case "ESTORNADO": return styles.bgRed;
    default: return {};
  }
}

function statusTextStyle(status: StatusLocacao | StatusPagamento) {
  switch (status) {
    case "RESERVADO": return styles.textBlue;
    case "RETIRADO": return styles.textYellow;
    case "DEVOLVIDO": return styles.textGreen;
    case "CANCELADO": return styles.textRed;
    case "PENDENTE": return styles.textYellow;
    case "PAGO": return styles.textGreen;
    case "ESTORNADO": return styles.textRed;
    default: return {};
  }
}

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
  },
  text: {
    fontSize: 12,
    fontWeight: "600",
  },
  bgBlue: { backgroundColor: "rgba(59,130,246,0.2)" },
  bgYellow: { backgroundColor: "rgba(234,179,8,0.2)" },
  bgGreen: { backgroundColor: "rgba(34,197,94,0.2)" },
  bgRed: { backgroundColor: "rgba(239,68,68,0.2)" },
  textBlue: { color: "#60a5fa" },
  textYellow: { color: "#facc15" },
  textGreen: { color: "#4ade80" },
  textRed: { color: "#f87171" },
});
