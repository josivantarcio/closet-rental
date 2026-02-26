package com.jtarcio.closetrental.modules.catalogo.infrastructure.mapper;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.infrastructure.persistence.ProdutoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    ProdutoEntity toEntity(Produto produto);

    Produto toDomain(ProdutoEntity entity);
}
