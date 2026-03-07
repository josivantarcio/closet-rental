package com.jtarcio.closetrental.modules.tenants;

import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tenant — política de cancelamento")
class TenantDomainTest {

    private Tenant tenant;
    private final LocalDate HOJE = LocalDate.now();

    // Base de cálculo: R$300 (ex: 3 dias * R$100/dia)
    private final BigDecimal BASE = new BigDecimal("300.00");

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        // usa defaults: 3d=30%, 1d=70%, 0d=100%
    }

    @Nested
    @DisplayName("calcularMultaCancelamento()")
    class CalcularMulta {

        @Test
        @DisplayName("deve retornar ZERO quando faltam 7 ou mais dias")
        void isentoAcimaDe7Dias() {
            LocalDate dataInicio = HOJE.plusDays(7);
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            assertThat(multa).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve retornar ZERO quando faltam exatamente 7 dias")
        void isentoExatamente7Dias() {
            LocalDate dataInicio = HOJE.plusDays(7);
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            assertThat(multa).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve aplicar 30% quando faltam exatamente 6 dias")
        void trintaPorCentoSeisDias() {
            LocalDate dataInicio = HOJE.plusDays(6);
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            // 300 * 30% = 90
            assertThat(multa).isEqualByComparingTo("90.00");
        }

        @Test
        @DisplayName("deve aplicar 30% quando faltam exatamente 3 dias")
        void trintaPorCentoTresDias() {
            LocalDate dataInicio = HOJE.plusDays(3);
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            assertThat(multa).isEqualByComparingTo("90.00");
        }

        @Test
        @DisplayName("deve aplicar 70% quando faltam 2 dias")
        void setentaPorCentoDoisDias() {
            LocalDate dataInicio = HOJE.plusDays(2);
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            // 300 * 70% = 210
            assertThat(multa).isEqualByComparingTo("210.00");
        }

        @Test
        @DisplayName("deve aplicar 70% quando falta 1 dia")
        void setentaPorCentoUmDia() {
            LocalDate dataInicio = HOJE.plusDays(1);
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            assertThat(multa).isEqualByComparingTo("210.00");
        }

        @Test
        @DisplayName("deve aplicar 100% quando é o mesmo dia (0 dias)")
        void cemPorCentoMesmoDia() {
            LocalDate dataInicio = HOJE;
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            // 300 * 100% = 300
            assertThat(multa).isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("deve respeitar percentuais customizados do tenant")
        void percentuaisCustomizados() {
            tenant.setPctCancelamento3d(new BigDecimal("50.00"));
            tenant.setPctCancelamento1d(new BigDecimal("80.00"));
            tenant.setPctCancelamento0d(new BigDecimal("100.00"));

            LocalDate dataInicio3d = HOJE.plusDays(4);
            assertThat(tenant.calcularMultaCancelamento(dataInicio3d, BASE, HOJE))
                    .isEqualByComparingTo("150.00"); // 300 * 50%

            LocalDate dataInicio1d = HOJE.plusDays(1);
            assertThat(tenant.calcularMultaCancelamento(dataInicio1d, BASE, HOJE))
                    .isEqualByComparingTo("240.00"); // 300 * 80%
        }

        @Test
        @DisplayName("deve retornar ZERO quando percentual da faixa é 0%")
        void percentualZeroCustomizado() {
            tenant.setPctCancelamento3d(BigDecimal.ZERO);
            LocalDate dataInicio = HOJE.plusDays(5);
            BigDecimal multa = tenant.calcularMultaCancelamento(dataInicio, BASE, HOJE);
            assertThat(multa).isEqualByComparingTo("0.00");
        }
    }
}
