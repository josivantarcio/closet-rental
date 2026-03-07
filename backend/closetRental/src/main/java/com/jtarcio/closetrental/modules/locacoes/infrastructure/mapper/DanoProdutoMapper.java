package com.jtarcio.closetrental.modules.locacoes.infrastructure.mapper;

import com.jtarcio.closetrental.modules.locacoes.domain.model.DanoProduto;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.DanoProdutoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DanoProdutoMapper {

    DanoProdutoEntity toEntity(DanoProduto dano);

    DanoProduto toDomain(DanoProdutoEntity entity);
}
