package mx.com.fredkor.apps.citamedica.core.application.service.impl;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.repository.SucursalRepository;
import mx.com.fredkor.apps.citamedica.core.application.mapper.MedicoSucursalMapper;
import mx.com.fredkor.apps.citamedica.core.application.service.MedicoSucursalService;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.MedicoSucursalEntity;
import mx.com.fredkor.apps.citamedica.core.persistence.repository.MedicoEspecialidadRepository;
import mx.com.fredkor.apps.citamedica.core.persistence.repository.MedicoSucursalRepository;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.MedicoSucursalRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoSucursalResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class MedicoSucursalServiceImp implements MedicoSucursalService {
    private final MedicoSucursalRepository medicoSucursalRepository;
    private final MedicoEspecialidadRepository medicoEspecialidadRepository;
    private final SucursalRepository sucursalRepository;
    private final MedicoSucursalMapper medicoSucursalMapper;

    @Override
    public PaginatedResponseDto<MedicoSucursalResponse> getMedicoSucursales(
            String filtro, int page, int size, String orderBy, String orderDir) {
        var sort = orderDir.equalsIgnoreCase("desc")
                ? Sort.by(orderBy).descending() : Sort.by(orderBy).ascending();
        var result = medicoSucursalRepository.findAll(filtro, PageRequest.of(page, size, sort));
        var content = result.getContent().stream().map(medicoSucursalMapper::toResponse).toList();
        return new PaginatedResponseDto<>(content, result.getTotalElements(), page, size);
    }

    @Override
    public MedicoSucursalResponse getById(Integer id) {
        return medicoSucursalRepository.findById(id).map(medicoSucursalMapper::toResponse).orElse(null);
    }

    @Override
    @Transactional
    public MedicoSucursalResponse create(MedicoSucursalRequest request) {
        var entity = new MedicoSucursalEntity();
        setRelations(entity, request);
        return medicoSucursalMapper.toResponse(medicoSucursalRepository.save(entity));
    }

    @Override
    @Transactional
    public MedicoSucursalResponse update(Integer id, MedicoSucursalRequest request) {
        var entity = medicoSucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico sucursal con ID " + id + " no existe."));
        setRelations(entity, request);
        return medicoSucursalMapper.toResponse(medicoSucursalRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!medicoSucursalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }
        medicoSucursalRepository.deleteById(id);
    }

    private void setRelations(MedicoSucursalEntity entity, MedicoSucursalRequest request) {
        entity.setMedicoEspecialidad(medicoEspecialidadRepository.findById(request.medicoEspecialidadId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Médico especialidad con ID " + request.medicoEspecialidadId() + " no existe.")));
        entity.setSucursal(sucursalRepository.findById(request.sucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal con ID " + request.sucursalId() + " no existe.")));
    }
}
