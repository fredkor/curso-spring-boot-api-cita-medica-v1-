package mx.com.fredkor.apps.citamedica.core.application.mapper;

import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EspecialidadResponse;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.MedicoEspecialidadEntity;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoEspecialidadResponse;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicoEspecialidadMapper {
    default MedicoEspecialidadResponse toResponse(MedicoEspecialidadEntity entity) {
        if (entity == null) return null;
        var especialidad = new EspecialidadResponse(
                entity.getEspecialidad().getId(), entity.getEspecialidad().getNombre());
        var medico = new MedicoResponse(
                entity.getMedico().getId(), entity.getMedico().getNombres(),
                entity.getMedico().getApellidoPaterno(), entity.getMedico().getApellidoMaterno(),
                List.of(especialidad));
        return new MedicoEspecialidadResponse(entity.getId(), medico, especialidad);
    }
}
