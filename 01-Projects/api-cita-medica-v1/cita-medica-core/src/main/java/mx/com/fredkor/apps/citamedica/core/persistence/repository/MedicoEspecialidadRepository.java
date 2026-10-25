package mx.com.fredkor.apps.citamedica.core.persistence.repository;

import mx.com.fredkor.apps.citamedica.core.persistence.entity.MedicoEspecialidadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoEspecialidadRepository extends JpaRepository<MedicoEspecialidadEntity, Integer> {

    @EntityGraph(attributePaths = {"medico", "especialidad"})
    @Query("""
            SELECT me FROM MedicoEspecialidadEntity me
            WHERE lower(concat(
                coalesce(me.medico.nombres, ''), ' ',
                coalesce(me.medico.apellidoPaterno, ''), ' ',
                coalesce(me.medico.apellidoMaterno, ''), ' ',
                coalesce(me.especialidad.nombre, '')
            )) LIKE lower(concat('%', :filtro, '%'))
            """)
    Page<MedicoEspecialidadEntity> findAll(String filtro, Pageable pageable);

}
