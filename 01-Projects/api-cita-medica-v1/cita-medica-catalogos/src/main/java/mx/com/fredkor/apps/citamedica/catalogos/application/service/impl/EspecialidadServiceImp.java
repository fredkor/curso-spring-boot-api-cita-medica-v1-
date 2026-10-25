package mx.com.fredkor.apps.citamedica.catalogos.application.service.impl;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.repository.EspecialidadRepository;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.EspecialidadRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EspecialidadResponse;
import mx.com.fredkor.apps.citamedica.catalogos.application.mapper.EspecialidadMapper;
import mx.com.fredkor.apps.citamedica.catalogos.application.service.EspecialidadService;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class EspecialidadServiceImp implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    private final EspecialidadMapper especialidadMapper;

    @Override
    public PaginatedResponseDto<EspecialidadResponse> getEspecialidades(String filtro, int page, int size, String orderBy, String orderDir) {
        var sort = orderDir.equalsIgnoreCase("desc")
                ? Sort.by(orderBy).descending()
                : Sort.by(orderBy).ascending();

        var pageable = PageRequest.of(page, size, sort);

        var pageResult = especialidadRepository.findAll(filtro, pageable) ;

        var content = pageResult.getContent()
                .stream()
                .map(especialidadMapper::toResponse)
                .toList();

        return new PaginatedResponseDto<>(
                content,
                pageResult.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public EspecialidadResponse getById(Integer id) {
        return especialidadRepository.findById(id)
                .map(especialidadMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public EspecialidadResponse create(EspecialidadRequest request) {
        var entity = especialidadMapper.toEntity(request);
        var claseSaved = especialidadRepository.save(entity);
        return especialidadMapper.toResponse(claseSaved);
    }

    @Override
    @Transactional
    public EspecialidadResponse update(Integer id, EspecialidadRequest request) {
        var entityFetched = especialidadRepository.findById(id)
                .map(entity -> {
                    especialidadMapper.entityToUpdate(request, entity);
                    return especialidadRepository.save(entity);
                }).orElseThrow(() -> new ResourceNotFoundException("Recurso con ID " + id + " no existe."));

        return especialidadMapper.toResponse(entityFetched);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!especialidadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }
        especialidadRepository.deleteById(id);
    }
}
