package com.jtarcio.closetrental.modules.clientes.infrastructure.mapper;

import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import com.jtarcio.closetrental.modules.clientes.infrastructure.persistence.ClienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper MapStruct entre domain {@link Cliente} e JPA {@link ClienteEntity}.
 * Gerado em tempo de compilação — sem reflexão em runtime.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClienteMapper {

    ClienteEntity toEntity(Cliente cliente);

    Cliente toDomain(ClienteEntity entity);
}
