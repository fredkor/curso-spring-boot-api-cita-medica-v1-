package mx.com.fredkor.apps.citamedica.usuario.application.service;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.request.UsuarioRequest;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.response.UsuarioResponse;
import mx.com.fredkor.apps.citamedica.usuario.application.mapper.UsuarioMapper;
import mx.com.fredkor.apps.citamedica.usuario.persistence.UsuarioConsultaRepository;
import mx.com.fredkor.security.persistence.entity.RolEntity;
import mx.com.fredkor.security.persistence.entity.UsuarioEntity;
import mx.com.fredkor.security.persistence.repository.RolRepository;
import mx.com.fredkor.security.persistence.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class UsuarioServiceImp implements UsuarioService {

    private final UsuarioConsultaRepository usuarioConsultaRepository;

    private final UsuarioRepository usuarioRepository;

    private final RolRepository rolRepository;

    private final UsuarioMapper usuarioMapper;

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasAuthority('USUARIO_VER')")
    public PaginatedResponseDto<UsuarioResponse> getUsuarios(String filtro, int page, int size, String orderBy, String orderDir) {
        var sort = orderDir.equalsIgnoreCase("desc")
                ? Sort.by(orderBy).descending()
                : Sort.by(orderBy).ascending();

        var pageable = PageRequest.of(page, size, sort);

        var pageResult = usuarioConsultaRepository.findUsuariosByPage(filtro, pageable);

        // La primera consulta pagina en SQL. Esta segunda inicializa las colecciones
        // de las mismas entidades gestionadas, sin alterar el orden de la página.
        if (!pageResult.isEmpty()) {
            usuarioConsultaRepository.cargarRolesPorIds(
                    pageResult.getContent().stream().map(UsuarioEntity::getId).toList());
        }

        var content = pageResult.getContent()
                .stream()
                .map(usuarioMapper::toResponse)
                .toList();

        return new PaginatedResponseDto<>(
                content,
                pageResult.getTotalElements(),
                page,
                size
        );
    }

    @Override
    @PreAuthorize("hasAuthority('USUARIO_VER')")
    public UsuarioResponse getById(Integer id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('USUARIO_CREAR')")
    public UsuarioResponse create(UsuarioRequest request) {
        var usuarioEntity = usuarioMapper.toEntity(request);
        usuarioEntity.setPassword(passwordEncoder.encode(request.password()));

        var usuarioRolEntities = request.roles()
                .stream()
                .map(idRol -> {
                    var rolEntity = rolRepository.findById(idRol)
                            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

                    return rolEntity;
                })
                .collect(Collectors.toSet());

        usuarioEntity.setRoles(usuarioRolEntities);

        var claseSaved = usuarioRepository.save(usuarioEntity);
        return usuarioMapper.toResponse(claseSaved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('USUARIO_MODIFICAR')")
    public UsuarioResponse update(Integer id, UsuarioRequest request) {
        var entityFetched = usuarioRepository.findById(id)
                .map(entity -> {
                    usuarioMapper.entityToUpdate(request, entity);
                    if (request.password() != null) {
                        entity.setPassword(passwordEncoder.encode(request.password()));
                    }
                    actualizarRoles(entity, request.roles());
                    return usuarioRepository.save(entity);
                }).orElseThrow(() -> new ResourceNotFoundException("Recurso con ID " + id + " no existe."));

        return usuarioMapper.toResponse(entityFetched);
    }

    private void actualizarRoles(
            UsuarioEntity usuario,
            List<Integer> nuevosRolesIds
    ) {
        Set<Integer> idsSolicitados = nuevosRolesIds == null
                ? Set.of()
                : new HashSet<>(nuevosRolesIds);

        if (rolRepository.findAllById(idsSolicitados).size() != idsSolicitados.size()) {
            throw new ResourceNotFoundException("Uno o más roles no existen");
        }

        usuario.getRoles().removeIf(
                usuarioRol -> !idsSolicitados.contains(
                        usuarioRol.getId()
                )
        );

        Set<Integer> idsActuales = usuario.getRoles().stream()
                .map(RolEntity::getId)
                .collect(Collectors.toSet());

        List<Integer> idsParaAgregar = idsSolicitados.stream()
                .filter(id -> !idsActuales.contains(id))
                .toList();

        List<RolEntity> rolesParaAgregar =
                rolRepository.findAllById(idsParaAgregar);
        usuario.getRoles().addAll(rolesParaAgregar);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('USUARIO_ELIMINAR')")
    public void delete(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
