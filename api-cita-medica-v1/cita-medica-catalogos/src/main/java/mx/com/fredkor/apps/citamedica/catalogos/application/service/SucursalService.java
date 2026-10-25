package mx.com.fredkor.apps.citamedica.catalogos.application.service;


import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalFilterRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.SucursalResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;

public interface SucursalService {
    PaginatedResponseDto<SucursalResponse> getSucursales(SucursalFilterRequest filtro, int page, int size, String orderBy, String orderDir);

    SucursalResponse getById(Integer id);

    SucursalResponse create(SucursalRequest request);

    SucursalResponse update(Integer id, SucursalRequest request);

    void delete(Integer id);
}
