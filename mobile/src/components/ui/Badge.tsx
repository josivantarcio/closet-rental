import { Text, View } from "react-native";
import type { StatusLocacao, StatusPagamento } from "@/types";

const locacaoColors: Record<StatusLocacao, string> = {
  RESERVADO: "bg-blue-500/20 text-blue-400",
  RETIRADO: "bg-yellow-500/20 text-yellow-400",
  DEVOLVIDO: "bg-green-500/20 text-green-400",
  CANCELADO: "bg-red-500/20 text-red-400",
};

const pagamentoColors: Record<StatusPagamento, string> = {
  PENDENTE: "bg-yellow-500/20 text-yellow-400",
  PAGO: "bg-green-500/20 text-green-400",
  ESTORNADO: "bg-red-500/20 text-red-400",
};

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
  const colors = type === "locacao"
    ? locacaoColors[status as StatusLocacao]
    : pagamentoColors[status as StatusPagamento];
  const label = type === "locacao"
    ? locacaoLabels[status as StatusLocacao]
    : pagamentoLabels[status as StatusPagamento];

  return (
    <View className={`px-2.5 py-1 rounded-full ${colors.split(" ")[0]}`}>
      <Text className={`text-xs font-semibold ${colors.split(" ")[1]}`}>{label}</Text>
    </View>
  );
}
