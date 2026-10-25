package mx.com.fredkor.apps.citamedica.catalogos.application.mapper;

import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.SucursalResponse;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.SucursalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SucursalMapper {
    SucursalResponse toResponse(SucursalEntity entity);

    SucursalEntity toEntity(SucursalRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    void entityToUpdate(SucursalRequest request, @MappingTarget SucursalEntity entity);
}
