package mx.com.fredkor.apps.citamedica.catalogos.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="estatus_cita")
public class EstatusCitaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "estatus_cita_sequence"
    )
    @SequenceGenerator(
            name = "estatus_cita_sequence",
            sequenceName = "seq_estatus_cita",
            allocationSize = 1
    )
    @Column(name = "id_estatus_cita")
    private Integer id;

    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

}
