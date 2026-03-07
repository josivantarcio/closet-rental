package com.jtarcio.closetrental.modules.locacoes.infrastructure.mapper;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.LocacaoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper MapStruct entre domain {@link Locacao} e JPA {@link LocacaoEntity}.
 * O campo {@code itens} é mapeado automaticamente via {@link ItemLocacaoMapper}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = ItemLocacaoMapper.class)
public interface LocacaoMapper {

    LocacaoEntity toEntity(Locacao locacao);

    Locacao toDomain(LocacaoEntity entity);
}
