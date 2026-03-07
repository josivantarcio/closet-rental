import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Alert, FlatList, Text, View } from "react-native";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { pagamentosService } from "@/lib/services/pagamentos.service";
import type { PagamentoResponse } from "@/types";
import { useState } from "react";

const metodoLabel: Record<string, string> = {
  DINHEIRO: "Dinheiro",
  CARTAO_CREDITO: "Crédito",
  CARTAO_DEBITO: "Débito",
  PIX: "PIX",
  TRANSFERENCIA: "Transferência",
};

function PagamentoCard({ pag, onEstornar }: { pag: PagamentoResponse; onEstornar: (id: number) => void }) {
  return (
    <Card className="gap-3">
      <View className="flex-row justify-between items-center">
        <View>
          <Text className="text-white font-semibold">Locação #{pag.locacaoId}</Text>
          <Text className="text-white/50 text-sm">{metodoLabel[pag.metodo] ?? pag.metodo}</Text>
        </View>
        <View className="items-end gap-1">
          <Text className="text-white font-bold text-lg">R$ {pag.valor.toFixed(2)}</Text>
          <Badge status={pag.status} type="pagamento" />
        </View>
      </View>
      {pag.status === "PAGO" && (
        <Button title="Estornar" size="sm" variant="danger" onPress={() => onEstornar(pag.id)} />
      )}
    </Card>
  );
}

export default function PagamentosScreen() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ["pagamentos", page],
    queryFn: () => pagamentosService.listar(page),
  });

  const estornarMutation = useMutation({
    mutationFn: pagamentosService.estornar,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["pagamentos"] }),
    onError: () => Alert.alert("Erro", "Não foi possível estornar o pagamento."),
  });

  const handleEstornar = (id: number) => {
    Alert.alert("Confirmar", "Deseja estornar este pagamento?", [
      { text: "Não", style: "cancel" },
      { text: "Sim", style: "destructive", onPress: () => estornarMutation.mutate(id) },
    ]);
  };

  const totalPago = data?.content.filter((p) => p.status === "PAGO").reduce((acc, p) => acc + p.valor, 0) ?? 0;

  return (
    <View className="flex-1 bg-surface">
      <View className="pt-14 px-4 pb-3 gap-3">
        <Text className="text-white text-2xl font-bold">Pagamentos</Text>
        <Card className="flex-row justify-between">
          <Text className="text-white/50">Total recebido (página)</Text>
          <Text className="text-green-400 font-bold">R$ {totalPago.toFixed(2)}</Text>
        </Card>
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => <PagamentoCard pag={item} onEstornar={handleEstornar} />}
        contentContainerStyle={{ padding: 16, gap: 12 }}
        ListEmptyComponent={
          isLoading
            ? <Text className="text-white/40 text-center mt-10">Carregando...</Text>
            : <Text className="text-white/40 text-center mt-10">Nenhum pagamento encontrado.</Text>
        }
        ListFooterComponent={
          data && !data.last ? (
            <Button title="Carregar mais" variant="outline" onPress={() => setPage((p) => p + 1)} loading={isFetching} />
          ) : null
        }
      />
    </View>
  );
}
