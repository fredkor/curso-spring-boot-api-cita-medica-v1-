package mx.com.fredkor.security.persistence.repository;

import mx.com.fredkor.security.persistence.entity.PermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<PermisoEntity, Integer> {
    Optional<PermisoEntity> findByClaveIgnoreCase(String clave);
}
