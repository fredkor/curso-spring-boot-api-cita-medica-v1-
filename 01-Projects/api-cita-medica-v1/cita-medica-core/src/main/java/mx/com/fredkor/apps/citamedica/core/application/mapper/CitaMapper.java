package mx.com.fredkor.apps.citamedica.core.application.mapper;

import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.CitaResponse;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.CitaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CitaMapper {
    CitaResponse toResponse(CitaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicoSucursal", ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "estatusCita", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    CitaEntity toEntity(CitaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "medicoSucursal", ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "estatusCita", ignore = true)
    void entityToUpdate(CitaRequest request, @MappingTarget CitaEntity entity);
}
