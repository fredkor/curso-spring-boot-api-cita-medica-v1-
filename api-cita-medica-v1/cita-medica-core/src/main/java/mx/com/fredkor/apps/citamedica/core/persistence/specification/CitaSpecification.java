package mx.com.fredkor.apps.citamedica.core.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaFilterRequest;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.CitaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CitaSpecification {

    public static Specification<CitaEntity> buscar(CitaFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.nombre() != null) {
                predicates.add(cb.like(
                        cb.upper(root.get("medicoSucursal").get("sucursal").get("nombre")), "%" + filter.nombre().toUpperCase() + "%")
                );
            }

            if (filter.direccion() != null) {
                predicates.add(cb.like(
                        cb.upper(root.get("medicoSucursal").get("sucursal").get("direccion")), "%" + filter.direccion().toUpperCase() + "%")
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
