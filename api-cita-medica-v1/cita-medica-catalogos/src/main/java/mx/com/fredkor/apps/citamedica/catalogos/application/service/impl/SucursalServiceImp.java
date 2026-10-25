package mx.com.fredkor.apps.citamedica.catalogos.application.service.impl;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalFilterRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.SucursalResponse;
import mx.com.fredkor.apps.citamedica.catalogos.application.mapper.SucursalMapper;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.repository.SucursalRepository;
import mx.com.fredkor.apps.citamedica.catalogos.application.service.SucursalService;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.specification.SucursalSpecification;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class SucursalServiceImp implements SucursalService {

    private final SucursalRepository sucursalRepository;

    private final SucursalMapper sucursalMapper;

    @Override
    public PaginatedResponseDto<SucursalResponse> getSucursales(SucursalFilterRequest filtro, int page, int size, String orderBy, String orderDir) {
        var sort = orderDir.equalsIgnoreCase("desc")
                ? Sort.by(orderBy).descending()
                : Sort.by(orderBy).ascending();

        var pageable = PageRequest.of(page, size, sort);

        var pageResult = sucursalRepository.findAll(SucursalSpecification.buscar(filtro), pageable) ;

        var content = pageResult.getContent()
                .stream()
                .map(sucursalMapper::toResponse)
                .toList();

        return new PaginatedResponseDto<>(
                content,
                pageResult.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public SucursalResponse getById(Integer id) {
        return sucursalRepository.findById(id)
                .map(sucursalMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public SucursalResponse create(SucursalRequest request) {
        var entity = sucursalMapper.toEntity(request);
        var claseSaved = sucursalRepository.save(entity);
        return sucursalMapper.toResponse(claseSaved);
    }

    @Override
    @Transactional
    public SucursalResponse update(Integer id, SucursalRequest request) {
        var entityFetched = sucursalRepository.findById(id)
                .map(entity -> {
                    sucursalMapper.entityToUpdate(request, entity);
                    return sucursalRepository.save(entity);
                }).orElseThrow(() -> new ResourceNotFoundException("Recurso con ID " + id + " no existe."));

        return sucursalMapper.toResponse(entityFetched);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!sucursalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }
        sucursalRepository.deleteById(id);
    }
}
