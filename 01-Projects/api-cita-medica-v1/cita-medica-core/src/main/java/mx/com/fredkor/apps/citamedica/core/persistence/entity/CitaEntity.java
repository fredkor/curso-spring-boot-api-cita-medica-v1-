package mx.com.fredkor.apps.citamedica.core.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EstatusCitaEntity;
import mx.com.fredkor.security.persistence.entity.UsuarioEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "cita")
public class CitaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "cita_sequence"
    )
    @SequenceGenerator(
            name = "cita_sequence",
            sequenceName = "seq_cita",
            allocationSize = 1
    )
    @Column(name = "id_cita")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_medico_sucursal")
    private MedicoSucursalEntity medicoSucursal;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private UsuarioEntity paciente;

    @ManyToOne
    @JoinColumn(name = "id_estatus_cita")
    private EstatusCitaEntity estatusCita;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_termino")
    private LocalDateTime fechaHoraTermino;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    private BigDecimal precio;

    private BigDecimal iva;

    private BigDecimal total;

}