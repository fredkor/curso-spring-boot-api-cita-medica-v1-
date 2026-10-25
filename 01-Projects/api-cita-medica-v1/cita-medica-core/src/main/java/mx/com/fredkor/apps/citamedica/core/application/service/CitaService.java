package mx.com.fredkor.apps.citamedica.core.application.service;


import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaFilterRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.CitaResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;

public interface CitaService {
    PaginatedResponseDto<CitaResponse> getCitas(CitaFilterRequest filtro, int page, int size, String orderBy, String orderDir);

    CitaResponse getById(Integer id);

    CitaResponse create(CitaRequest request);

    CitaResponse update(Integer id, CitaRequest request);

    void delete(Integer id);

    CitaResponse confirmar(Integer id);

    CitaResponse cancelar(Integer id);

    CitaResponse atender(Integer id);

    int cancelarCitasNoConfirmadas();

}
