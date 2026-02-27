package com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence.projection;

import java.math.BigDecimal;

/**
 * Projeção interface para a query de faturamento (SUM + COUNT).
 * Spring Data instancia automaticamente via proxy.
 */
public interface FaturamentoProjection {
    BigDecimal getTotalReceita();
    Long getTotalPagamentos();
}
