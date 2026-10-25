package mx.com.fredkor.apps.citamedica.core.application.service;

import mx.com.fredkor.apps.citamedica.core.web.dto.request.MedicoSucursalRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoSucursalResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;

public interface MedicoSucursalService {
    PaginatedResponseDto<MedicoSucursalResponse> getMedicoSucursales(
            String filtro, int page, int size, String orderBy, String orderDir);
    MedicoSucursalResponse getById(Integer id);
    MedicoSucursalResponse create(MedicoSucursalRequest request);
    MedicoSucursalResponse update(Integer id, MedicoSucursalRequest request);
    void delete(Integer id);
}
