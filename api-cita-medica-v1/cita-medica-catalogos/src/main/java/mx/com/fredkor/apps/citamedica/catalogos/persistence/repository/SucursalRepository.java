package mx.com.fredkor.apps.citamedica.catalogos.persistence.repository;

import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.SucursalEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SucursalRepository extends JpaRepository<SucursalEntity, Integer>, JpaSpecificationExecutor<SucursalEntity> {

    @Query("SELECT c FROM "
            + "SucursalEntity c "
            + "WHERE lower(c.nombre) LIKE lower(concat('%', :filtro, '%'))"
            + " OR lower(c.direccion) LIKE lower(concat('%', :filtro, '%'))")
    Page<SucursalEntity> findSucursalesByPage(String filtro, Pageable pageable);

    Page<SucursalEntity> findAll(@NonNull Specification<SucursalEntity> spec, @NonNull Pageable pageable);
}
