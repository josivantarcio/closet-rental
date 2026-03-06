package com.jtarcio.closetrental.modules.tenants.infrastructure.mapper;

import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.infrastructure.persistence.TenantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TenantEntity toEntity(Tenant tenant);

    Tenant toDomain(TenantEntity entity);
}
