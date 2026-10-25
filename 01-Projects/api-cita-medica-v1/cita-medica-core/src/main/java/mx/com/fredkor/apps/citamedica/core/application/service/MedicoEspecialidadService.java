package mx.com.fredkor.apps.citamedica.core.application.service;

import mx.com.fredkor.apps.citamedica.core.web.dto.request.MedicoEspecialidadRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoEspecialidadResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;

public interface MedicoEspecialidadService {
    PaginatedResponseDto<MedicoEspecialidadResponse> getMedicoEspecialidades(
            String filtro, int page, int size, String orderBy, String orderDir);
    MedicoEspecialidadResponse getById(Integer id);
    MedicoEspecialidadResponse create(MedicoEspecialidadRequest request);
    MedicoEspecialidadResponse update(Integer id, MedicoEspecialidadRequest request);
    void delete(Integer id);
}
