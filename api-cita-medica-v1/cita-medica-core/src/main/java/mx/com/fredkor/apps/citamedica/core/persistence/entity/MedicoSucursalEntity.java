package mx.com.fredkor.apps.citamedica.core.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.SucursalEntity;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="medico_sucursal")
public class MedicoSucursalEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "medico_sucursal_sequence"
    )
    @SequenceGenerator(
            name = "medico_sucursal_sequence",
            sequenceName = "seq_medico_sucursal",
            allocationSize = 1
    )
    @Column(name = "id_medico_sucursal")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico_especialidad", nullable = false)
    private MedicoEspecialidadEntity medicoEspecialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private SucursalEntity sucursal;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

}
