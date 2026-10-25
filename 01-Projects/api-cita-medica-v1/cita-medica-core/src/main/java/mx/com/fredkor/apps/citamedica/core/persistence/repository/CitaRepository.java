package mx.com.fredkor.apps.citamedica.core.persistence.repository;

import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EstatusCitaEntity;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.CitaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface CitaRepository extends JpaRepository<CitaEntity, Integer>, JpaSpecificationExecutor<CitaEntity> {

    @EntityGraph(attributePaths = {
            "paciente",
            "estatusCita",
            "medicoSucursal.medicoEspecialidad.medico",
            "medicoSucursal.medicoEspecialidad.especialidad",
            "medicoSucursal.sucursal"
    })
    Page<CitaEntity> findAll(@NonNull Specification<CitaEntity> spec, @NonNull Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CitaEntity c WHERE c.id = :id")
    Optional<CitaEntity> findByIdForUpdate(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE CitaEntity c
           SET c.estatusCita = :estatusCancelada,
               c.fechaModificacion = :fechaModificacion
         WHERE c.estatusCita = :estatusReservada
           AND c.fechaHoraInicio <= :limiteConfirmacion
        """)
    int cancelarCitasNoConfirmadas(
            @Param("estatusReservada")
            EstatusCitaEntity estatusReservada,

            @Param("estatusCancelada")
            EstatusCitaEntity estatusCancelada,

            @Param("limiteConfirmacion")
            LocalDateTime limiteConfirmacion,

            @Param("fechaModificacion")
            LocalDateTime fechaModificacion
    );
}
