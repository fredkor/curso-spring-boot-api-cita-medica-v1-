package mx.com.fredkor.apps.citamedica.core.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EspecialidadEntity;
import mx.com.fredkor.security.persistence.entity.UsuarioEntity;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="medico_especialidad")
public class MedicoEspecialidadEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "medico_especialidad_sequence"
    )
    @SequenceGenerator(
            name = "medico_especialidad_sequence",
            sequenceName = "seq_medico_especialidad",
            allocationSize = 1
    )
    @Column(name = "id_medico_especialidad")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private EspecialidadEntity especialidad;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

}
