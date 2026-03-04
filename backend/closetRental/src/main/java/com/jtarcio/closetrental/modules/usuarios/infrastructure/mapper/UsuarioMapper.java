package com.jtarcio.closetrental.modules.usuarios.infrastructure.mapper;

import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import com.jtarcio.closetrental.modules.usuarios.infrastructure.persistence.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UsuarioEntity toEntity(Usuario usuario);

    Usuario toDomain(UsuarioEntity entity);
}
