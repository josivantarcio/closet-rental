package com.jtarcio.closetrental.modules.pagamentos.infrastructure.mapper;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.infrastructure.persistence.PagamentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper MapStruct entre domain {@link Pagamento} e JPA {@link PagamentoEntity}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PagamentoMapper {

    PagamentoEntity toEntity(Pagamento pagamento);

    Pagamento toDomain(PagamentoEntity entity);
}
