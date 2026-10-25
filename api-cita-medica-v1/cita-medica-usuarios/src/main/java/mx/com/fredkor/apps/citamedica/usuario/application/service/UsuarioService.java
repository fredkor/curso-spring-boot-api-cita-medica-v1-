package mx.com.fredkor.apps.citamedica.usuario.application.service;


import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.request.UsuarioRequest;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.response.UsuarioResponse;

public interface UsuarioService {
    PaginatedResponseDto<UsuarioResponse> getUsuarios(String filtro, int page, int size, String orderBy, String orderDir);

    UsuarioResponse getById(Integer id);

    UsuarioResponse create(UsuarioRequest request);

    UsuarioResponse update(Integer id, UsuarioRequest request);

    void delete(Integer id);
}
