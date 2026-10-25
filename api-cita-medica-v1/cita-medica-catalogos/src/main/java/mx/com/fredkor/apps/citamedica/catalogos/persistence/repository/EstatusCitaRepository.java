package mx.com.fredkor.apps.citamedica.catalogos.persistence.repository;

import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EstatusCitaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstatusCitaRepository extends JpaRepository<EstatusCitaEntity, Integer> {

    @Query("SELECT c FROM "
            + "EstatusCitaEntity c "
            + "WHERE lower(c.clave) LIKE lower(concat('%', :filtro, '%'))")
    Page<EstatusCitaEntity> findAll(String filtro, Pageable pageable);

    Optional<EstatusCitaEntity> findByClave(String clave);

}
