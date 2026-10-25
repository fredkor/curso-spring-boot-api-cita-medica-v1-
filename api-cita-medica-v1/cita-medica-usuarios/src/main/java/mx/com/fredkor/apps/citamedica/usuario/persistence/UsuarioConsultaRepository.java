package mx.com.fredkor.apps.citamedica.usuario.persistence;

import mx.com.fredkor.security.persistence.entity.UsuarioEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


public interface UsuarioConsultaRepository
        extends Repository<UsuarioEntity, Integer> {

    @Query("""
        SELECT u
        FROM UsuarioEntity u
        WHERE LOWER(u.nombres)
            LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(u.apellidoPaterno)
            LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(COALESCE(u.apellidoMaterno, ''))
            LIKE LOWER(CONCAT('%', :filtro, '%'))
        """)
    Page<UsuarioEntity> findUsuariosByPage(
            @Param("filtro") String filtro,
            Pageable pageable
    );

    // Se ejecuta sin Pageable, únicamente para los usuarios de la página obtenida.
    @Query("SELECT DISTINCT u FROM UsuarioEntity u LEFT JOIN FETCH u.roles WHERE u.id IN :ids")
    List<UsuarioEntity> cargarRolesPorIds(@Param("ids") List<Integer> ids);
}