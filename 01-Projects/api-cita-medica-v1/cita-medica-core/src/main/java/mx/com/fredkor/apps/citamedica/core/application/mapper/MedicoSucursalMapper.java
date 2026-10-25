package mx.com.fredkor.apps.citamedica.core.application.mapper;

import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.SucursalResponse;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.MedicoSucursalEntity;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoSucursalResponse;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class MedicoSucursalMapper {
    private final MedicoEspecialidadMapper medicoEspecialidadMapper;

    public MedicoSucursalResponse toResponse(MedicoSucursalEntity entity) {
        if (entity == null) return null;
        var sucursal = new SucursalResponse(
                entity.getSucursal().getId(), entity.getSucursal().getNombre(), entity.getSucursal().getDireccion());
        return new MedicoSucursalResponse(
                entity.getId(), sucursal, medicoEspecialidadMapper.toResponse(entity.getMedicoEspecialidad()));
    }
}
