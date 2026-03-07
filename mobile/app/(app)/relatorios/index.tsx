import { useQuery } from "@tanstack/react-query";
import { ScrollView, Text, View } from "react-native";
import { Card } from "@/components/ui/Card";
import { relatoriosService } from "@/lib/services/relatorios.service";

export default function RelatoriosScreen() {
  const hoje = new Date();
  const inicio = new Date(hoje.getFullYear(), hoje.getMonth() - 5, 1).toISOString().split("T")[0];
  const fim = hoje.toISOString().split("T")[0];

  const { data: faturamento } = useQuery({
    queryKey: ["relatorios", "faturamento"],
    queryFn: () => relatoriosService.faturamento(inicio, fim),
  });

  const { data: pecas } = useQuery({
    queryKey: ["relatorios", "pecas"],
    queryFn: () => relatoriosService.pecasMaisAlugadas(inicio, fim, 10),
  });

  const { data: atrasos } = useQuery({
    queryKey: ["relatorios", "atrasos"],
    queryFn: () => relatoriosService.atrasos(),
  });

  const totalReceita = faturamento?.reduce((acc, m) => acc + m.receitaBruta, 0) ?? 0;
  const totalLocacoes = faturamento?.reduce((acc, m) => acc + m.totalLocacoes, 0) ?? 0;

  return (
    <ScrollView className="flex-1 bg-surface" contentContainerStyle={{ padding: 16, gap: 16 }}>
      <View className="pt-10 pb-2">
        <Text className="text-white text-2xl font-bold">Relatórios</Text>
        <Text className="text-white/40 text-sm">Últimos 6 meses</Text>
      </View>

      {/* KPIs gerais */}
      <View className="flex-row gap-3">
        <Card className="flex-1 items-center gap-1">
          <Text className="text-green-400 text-xl font-bold">R$ {totalReceita.toFixed(0)}</Text>
          <Text className="text-white/40 text-xs text-center">Receita total</Text>
        </Card>
        <Card className="flex-1 items-center gap-1">
          <Text className="text-blue-400 text-xl font-bold">{totalLocacoes}</Text>
          <Text className="text-white/40 text-xs text-center">Locações</Text>
        </Card>
        <Card className="flex-1 items-center gap-1">
          <Text className="text-red-400 text-xl font-bold">{atrasos?.length ?? 0}</Text>
          <Text className="text-white/40 text-xs text-center">Em atraso</Text>
        </Card>
      </View>

      {/* Faturamento mensal */}
      <Text className="text-white font-semibold text-lg">Faturamento por mês</Text>
      {faturamento?.map((m) => (
        <Card key={m.mes} className="flex-row justify-between items-center">
          <Text className="text-white/70">{m.mes}</Text>
          <View className="items-end">
            <Text className="text-white font-semibold">R$ {m.receitaBruta.toFixed(2)}</Text>
            <Text className="text-white/40 text-xs">{m.totalLocacoes} locações</Text>
          </View>
        </Card>
      ))}

      {/* Peças mais alugadas */}
      <Text className="text-white font-semibold text-lg mt-2">Top 10 peças</Text>
      {pecas?.map((p, i) => (
        <Card key={p.produtoId} className="flex-row items-center gap-3">
          <Text className="text-primary font-bold text-lg w-6">{i + 1}</Text>
          <View className="flex-1">
            <Text className="text-white font-medium" numberOfLines={1}>{p.nomeProduto}</Text>
            <Text className="text-white/40 text-xs">{p.totalLocacoes} locações</Text>
          </View>
          <Text className="text-green-400 font-semibold">R$ {p.receitaTotal.toFixed(0)}</Text>
        </Card>
      ))}

      {/* Atrasos */}
      {(atrasos?.length ?? 0) > 0 && (
        <>
          <Text className="text-white font-semibold text-lg mt-2">Atrasos pendentes</Text>
          {atrasos?.map((a) => (
            <Card key={a.locacaoId} className="border-red-500/30 gap-1">
              <Text className="text-white font-medium">{a.clienteNome}</Text>
              <Text className="text-white/50 text-sm">Locação #{a.locacaoId} · {a.diasAtraso} dias</Text>
              <Text className="text-red-400 font-semibold">Multa: R$ {a.multaEstimada.toFixed(2)}</Text>
            </Card>
          ))}
        </>
      )}
    </ScrollView>
  );
}
