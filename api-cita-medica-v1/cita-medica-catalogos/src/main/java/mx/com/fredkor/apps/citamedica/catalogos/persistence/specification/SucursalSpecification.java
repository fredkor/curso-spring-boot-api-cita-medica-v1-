package mx.com.fredkor.apps.citamedica.catalogos.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalFilterRequest;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.SucursalEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SucursalSpecification {

    public static Specification<SucursalEntity> buscar(SucursalFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.nombre() != null) {
                predicates.add(cb.like(
                        cb.upper(root.get("nombre")), "%" + filter.nombre().toUpperCase() + "%")
                );
            }

            if (filter.direccion() != null) {
                predicates.add(cb.like(
                        cb.upper(root.get("direccion")), "%" + filter.direccion().toUpperCase() + "%")
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
