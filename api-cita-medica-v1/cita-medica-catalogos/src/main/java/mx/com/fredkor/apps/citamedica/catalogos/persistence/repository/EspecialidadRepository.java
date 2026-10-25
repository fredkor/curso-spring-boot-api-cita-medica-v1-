package mx.com.fredkor.apps.citamedica.catalogos.persistence.repository;

import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EspecialidadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadRepository extends JpaRepository<EspecialidadEntity, Integer> {

    @Query("SELECT c FROM "
            + "EspecialidadEntity c "
            + "WHERE lower(c.nombre) LIKE lower(concat('%', :filtro, '%'))")
    Page<EspecialidadEntity> findAll(String filtro, Pageable pageable);

}
