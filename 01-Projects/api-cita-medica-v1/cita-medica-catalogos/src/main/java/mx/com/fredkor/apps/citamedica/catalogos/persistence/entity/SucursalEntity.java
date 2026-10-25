package mx.com.fredkor.apps.citamedica.catalogos.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="sucursal")
public class SucursalEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sucursal_sequence"
    )
    @SequenceGenerator(
            name = "sucursal_sequence",
            sequenceName = "seq_sucursal",
            allocationSize = 1
    )
    @Column(name = "id_sucursal")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "direccion", nullable = false, length = 500)
    private String direccion;

    @Column(name="fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    private void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (activo == null) {
            activo = true;
        }
    }

}
