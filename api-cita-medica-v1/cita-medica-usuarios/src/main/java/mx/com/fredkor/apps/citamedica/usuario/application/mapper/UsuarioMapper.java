package mx.com.fredkor.apps.citamedica.usuario.application.mapper;

import mx.com.fredkor.apps.citamedica.usuario.web.dto.request.UsuarioRequest;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.response.UsuarioResponse;
import mx.com.fredkor.security.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponse toResponse(UsuarioEntity entity);

    @Mapping(target = "roles", ignore = true)
    UsuarioEntity toEntity(UsuarioRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void entityToUpdate(UsuarioRequest request, @MappingTarget UsuarioEntity entity);
}
