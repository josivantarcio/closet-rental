package com.jtarcio.closetrental.modules.estoque.infrastructure.mapper;

import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.infrastructure.persistence.MovimentacaoEstoqueEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper MapStruct entre domain {@link MovimentacaoEstoque} e JPA {@link MovimentacaoEstoqueEntity}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovimentacaoEstoqueMapper {

    MovimentacaoEstoqueEntity toEntity(MovimentacaoEstoque movimentacao);

    MovimentacaoEstoque toDomain(MovimentacaoEstoqueEntity entity);
}
