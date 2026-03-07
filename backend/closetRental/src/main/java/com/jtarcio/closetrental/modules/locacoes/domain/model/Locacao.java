package com.jtarcio.closetrental.modules.locacoes.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root do domínio de locações.
 * Encapsula todo o ciclo de vida: reserva, retirada, devolução e cancelamento.
 * Suporta múltiplos produtos por locação via List<ItemLocacao>.
 * Nenhuma dependência de Spring ou JPA.
 */
public class Locacao {

    private Long id;
    private UUID tenantId;
    private Long clienteId;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    private StatusLocacao status;

    private List<ItemLocacao> itens = new ArrayList<>();

    private BigDecimal caucao;
    private StatusCaucao statusCaucao = StatusCaucao.PENDENTE;
    private BigDecimal caucaoRetida;
    private BigDecimal multa;
    private BigDecimal multaCancelamento;
    private BigDecimal valorTotal;

    private LocalDateTime retiradaAt;
    private LocalDateTime devolucaoAt;

    private String observacoes;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Locacao() {}

    // ─────────────────────────────────────────────────────────────────
    // Regras de negócio de domínio
    // ─────────────────────────────────────────────────────────────────

    /**
     * Calcula o número de dias da locação (inclusivo nos dois extremos).
     * Ex: 10/dez a 12/dez = 3 dias.
     * Complexidade: O(1).
     */
    public long calcularDias() {
        return ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    /**
     * Calcula o valor total: soma dos subtotais dos itens + caução.
     * Subtotal de cada item = valorDiaria * dias.
     * Complexidade: O(n) onde n = número de itens (tipicamente pequeno, ≤ 10).
     */
    public BigDecimal calcularValorTotal() {
        long dias = calcularDias();
        BigDecimal somaItens = itens.stream()
                .map(item -> item.calcularSubtotal(dias))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return somaItens
                .add(caucao != null ? caucao : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a multa por atraso.
     * multa = percentualDiario * soma(valorDiaria dos itens) * diasAtraso.
     * Retorna ZERO se não há atraso.
     * Complexidade: O(n) onde n = número de itens.
     *
     * @param devolucaoReal data/hora real da devolução
     * @param percentualMultaDiaria percentual sobre a diária por dia de atraso (ex: 0.10 = 10%)
     */
    public BigDecimal calcularMulta(LocalDateTime devolucaoReal, BigDecimal percentualMultaDiaria) {
        if (devolucaoReal == null || !devolucaoReal.toLocalDate().isAfter(dataFim)) {
            return BigDecimal.ZERO;
        }
        long diasAtraso = ChronoUnit.DAYS.between(dataFim, devolucaoReal.toLocalDate());
        BigDecimal somadiarias = itens.stream()
                .map(ItemLocacao::getValorDiaria)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return percentualMultaDiaria
                .multiply(somadiarias)
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retorna IDs de todos os produtos na locação.
     * Complexidade: O(n).
     */
    public List<Long> getProdutoIds() {
        return itens.stream().map(ItemLocacao::getProdutoId).toList();
    }

    /**
     * Aplica o destino da caução na devolução.
     *
     * Regras:
     * - valorRetencao == null ou 0  → LIBERADA (sem retenção)
     * - 0 < valorRetencao < caucao  → RETIDA_PARCIAL
     * - valorRetencao >= caucao     → RETIDA_TOTAL (retém o teto = caucao)
     *
     * @param valorRetencao valor a reter da caução (null = liberar tudo)
     */
    public void aplicarDestinoCaucao(BigDecimal valorRetencao) {
        if (caucao == null || caucao.compareTo(BigDecimal.ZERO) == 0) {
            statusCaucao = StatusCaucao.LIBERADA;
            caucaoRetida = BigDecimal.ZERO;
            return;
        }
        if (valorRetencao == null || valorRetencao.compareTo(BigDecimal.ZERO) <= 0) {
            statusCaucao = StatusCaucao.LIBERADA;
            caucaoRetida = BigDecimal.ZERO;
        } else if (valorRetencao.compareTo(caucao) >= 0) {
            statusCaucao = StatusCaucao.RETIDA_TOTAL;
            caucaoRetida = caucao;
        } else {
            statusCaucao = StatusCaucao.RETIDA_PARCIAL;
            caucaoRetida = valorRetencao.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Verifica se a locação pode ser cancelada (apenas no estado RESERVADO).
     */
    public boolean podeCancelar() {
        return StatusLocacao.RESERVADO.equals(this.status);
    }

    /**
     * Verifica se a locação pode ser retirada (apenas no estado RESERVADO).
     */
    public boolean podeRetirar() {
        return StatusLocacao.RESERVADO.equals(this.status);
    }

    /**
     * Verifica se a locação pode ser devolvida (apenas no estado RETIRADO).
     */
    public boolean podeDevolver() {
        return StatusLocacao.RETIRADO.equals(this.status);
    }

    /**
     * Verifica se a devolução está atrasada em relação a uma data/hora de devolução real.
     */
    public boolean estaAtrasada(LocalDateTime devolucaoReal) {
        return devolucaoReal != null && devolucaoReal.toLocalDate().isAfter(dataFim);
    }

    // ─────────────────────────────────────────────────────────────────
    // Getters e Setters
    // ─────────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public StatusLocacao getStatus() { return status; }
    public void setStatus(StatusLocacao status) { this.status = status; }

    public List<ItemLocacao> getItens() { return itens; }
    public void setItens(List<ItemLocacao> itens) { this.itens = itens != null ? itens : new ArrayList<>(); }

    public BigDecimal getCaucao() { return caucao; }
    public void setCaucao(BigDecimal caucao) { this.caucao = caucao; }

    public StatusCaucao getStatusCaucao() { return statusCaucao; }
    public void setStatusCaucao(StatusCaucao statusCaucao) { this.statusCaucao = statusCaucao; }

    public BigDecimal getCaucaoRetida() { return caucaoRetida; }
    public void setCaucaoRetida(BigDecimal caucaoRetida) { this.caucaoRetida = caucaoRetida; }

    public BigDecimal getMulta() { return multa; }
    public void setMulta(BigDecimal multa) { this.multa = multa; }

    public BigDecimal getMultaCancelamento() { return multaCancelamento; }
    public void setMultaCancelamento(BigDecimal multaCancelamento) { this.multaCancelamento = multaCancelamento; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public LocalDateTime getRetiradaAt() { return retiradaAt; }
    public void setRetiradaAt(LocalDateTime retiradaAt) { this.retiradaAt = retiradaAt; }

    public LocalDateTime getDevolucaoAt() { return devolucaoAt; }
    public void setDevolucaoAt(LocalDateTime devolucaoAt) { this.devolucaoAt = devolucaoAt; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
