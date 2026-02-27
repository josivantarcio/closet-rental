package com.jtarcio.closetrental.modules.locacoes.infrastructure.mapper;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.LocacaoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper MapStruct entre domain {@link Locacao} e JPA {@link LocacaoEntity}.
 * Todos os campos têm mesmo nome — sem necessidade de @Mapping explícito.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocacaoMapper {

    LocacaoEntity toEntity(Locacao locacao);

    Locacao toDomain(LocacaoEntity entity);
}
