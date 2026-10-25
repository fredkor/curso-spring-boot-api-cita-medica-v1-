package mx.com.fredkor.apps.citamedica.core.application.service.impl;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.repository.EspecialidadRepository;
import mx.com.fredkor.apps.citamedica.core.application.mapper.MedicoEspecialidadMapper;
import mx.com.fredkor.apps.citamedica.core.application.service.MedicoEspecialidadService;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.MedicoEspecialidadEntity;
import mx.com.fredkor.apps.citamedica.core.persistence.repository.MedicoEspecialidadRepository;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.MedicoEspecialidadRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoEspecialidadResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import mx.com.fredkor.security.persistence.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class MedicoEspecialidadServiceImp implements MedicoEspecialidadService {
    private final MedicoEspecialidadRepository medicoEspecialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final MedicoEspecialidadMapper medicoEspecialidadMapper;

    @Override
    public PaginatedResponseDto<MedicoEspecialidadResponse> getMedicoEspecialidades(
            String filtro, int page, int size, String orderBy, String orderDir) {
        var sort = orderDir.equalsIgnoreCase("desc")
                ? Sort.by(orderBy).descending() : Sort.by(orderBy).ascending();
        var result = medicoEspecialidadRepository.findAll(filtro, PageRequest.of(page, size, sort));
        var content = result.getContent().stream().map(medicoEspecialidadMapper::toResponse).toList();
        return new PaginatedResponseDto<>(content, result.getTotalElements(), page, size);
    }

    @Override
    public MedicoEspecialidadResponse getById(Integer id) {
        return medicoEspecialidadRepository.findById(id)
                .map(medicoEspecialidadMapper::toResponse).orElse(null);
    }

    @Override
    @Transactional
    public MedicoEspecialidadResponse create(MedicoEspecialidadRequest request) {
        var entity = new MedicoEspecialidadEntity();
        setRelations(entity, request);
        return medicoEspecialidadMapper.toResponse(medicoEspecialidadRepository.save(entity));
    }

    @Override
    @Transactional
    public MedicoEspecialidadResponse update(Integer id, MedicoEspecialidadRequest request) {
        var entity = medicoEspecialidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico especialidad con ID " + id + " no existe."));
        setRelations(entity, request);
        return medicoEspecialidadMapper.toResponse(medicoEspecialidadRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!medicoEspecialidadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }
        medicoEspecialidadRepository.deleteById(id);
    }

    private void setRelations(MedicoEspecialidadEntity entity, MedicoEspecialidadRequest request) {
        entity.setMedico(usuarioRepository.findById(request.medicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico con ID " + request.medicoId() + " no existe.")));
        entity.setEspecialidad(especialidadRepository.findById(request.especialidadId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Especialidad con ID " + request.especialidadId() + " no existe.")));
    }
}
