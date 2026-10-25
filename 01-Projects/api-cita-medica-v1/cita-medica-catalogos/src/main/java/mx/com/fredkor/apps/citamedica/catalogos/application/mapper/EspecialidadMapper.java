package mx.com.fredkor.apps.citamedica.catalogos.application.mapper;

import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.EspecialidadRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EspecialidadResponse;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EspecialidadEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EspecialidadMapper {
    EspecialidadResponse toResponse(EspecialidadEntity entity);

    EspecialidadEntity toEntity(EspecialidadRequest request);

    @Mapping(target = "id", ignore = true)
    void entityToUpdate(EspecialidadRequest request, @MappingTarget EspecialidadEntity entity);
}
