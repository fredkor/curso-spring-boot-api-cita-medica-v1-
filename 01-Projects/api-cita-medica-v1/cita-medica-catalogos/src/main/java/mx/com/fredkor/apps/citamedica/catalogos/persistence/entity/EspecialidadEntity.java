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
@Table(name="especialidad")
public class EspecialidadEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "especialidad_sequence"
    )
    @SequenceGenerator(
            name = "especialidad_sequence",
            sequenceName = "seq_especialidad",
            allocationSize = 1
    )
    @Column(name = "id_especialidad")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    private void prePersist() {
        if (activo == null) {
            activo = true;
        }
    }

}
