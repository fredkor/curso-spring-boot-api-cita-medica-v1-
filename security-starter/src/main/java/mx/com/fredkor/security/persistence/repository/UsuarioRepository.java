package mx.com.fredkor.security.persistence.repository;

import mx.com.fredkor.security.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    @EntityGraph(attributePaths = {"roles", "roles.permisos"})
    Optional<UsuarioEntity> findByUsernameIgnoreCase(String username);
}
