package mx.com.fredkor.apps.citamedica.core.persistence.repository;

import mx.com.fredkor.apps.citamedica.core.persistence.entity.MedicoSucursalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoSucursalRepository extends JpaRepository<MedicoSucursalEntity, Integer> {

    @EntityGraph(attributePaths = {"sucursal", "medicoEspecialidad.medico", "medicoEspecialidad.especialidad"})
    @Query("""
            SELECT ms FROM MedicoSucursalEntity ms
            WHERE lower(concat(
                coalesce(ms.medicoEspecialidad.medico.nombres, ''), ' ',
                coalesce(ms.medicoEspecialidad.medico.apellidoPaterno, ''), ' ',
                coalesce(ms.medicoEspecialidad.medico.apellidoMaterno, ''), ' ',
                coalesce(ms.medicoEspecialidad.especialidad.nombre, ''), ' ',
                coalesce(ms.sucursal.nombre, '')
            )) LIKE lower(concat('%', :filtro, '%'))
            """)
    Page<MedicoSucursalEntity> findAll(String filtro, Pageable pageable);

}
