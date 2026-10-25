package mx.com.fredkor.security.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permiso")
public class PermisoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permiso_sequence")
    @SequenceGenerator(name = "permiso_sequence", sequenceName = "seq_permiso", allocationSize = 1)
    @Column(name = "id_permiso")
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String clave;

    @Column(nullable = false, length = 100)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

}
