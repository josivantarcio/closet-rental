package com.jtarcio.closetrental.modules.locacoes.infrastructure.mapper;

import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.ItemLocacaoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper MapStruct entre domain {@link ItemLocacao} e JPA {@link ItemLocacaoEntity}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemLocacaoMapper {

    ItemLocacaoEntity toEntity(ItemLocacao item);

    ItemLocacao toDomain(ItemLocacaoEntity entity);
}
