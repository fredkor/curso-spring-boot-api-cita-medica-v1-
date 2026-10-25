package mx.com.fredkor.security.persistence.repository;

import mx.com.fredkor.security.persistence.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<RolEntity, Integer> {
    Optional<RolEntity> findByClaveIgnoreCase(String clave);
}
