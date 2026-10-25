package mx.com.fredkor.apps.citamedica.catalogos.application.service;


import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.EspecialidadRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EspecialidadResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;

public interface EspecialidadService {
    PaginatedResponseDto<EspecialidadResponse> getEspecialidades(String filtro, int page, int size, String orderBy, String orderDir);

    EspecialidadResponse getById(Integer id);

    EspecialidadResponse create(EspecialidadRequest request);

    EspecialidadResponse update(Integer id, EspecialidadRequest request);

    void delete(Integer id);
}
