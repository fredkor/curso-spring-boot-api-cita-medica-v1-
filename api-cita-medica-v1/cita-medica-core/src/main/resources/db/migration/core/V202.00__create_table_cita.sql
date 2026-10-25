CREATE SEQUENCE citamedica.seq_cita
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    NO CYCLE;

CREATE TABLE citamedica.cita (
    id_cita INTEGER NOT NULL DEFAULT nextval('citamedica.seq_cita'::regclass),
    id_medico_sucursal integer NOT NULL,
    id_usuario integer NOT NULL,
    id_estatus_cita integer NOT NULL,
    fecha_hora_inicio TIMESTAMP NOT NULL,
    fecha_hora_termino TIMESTAMP NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    precio DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,

    CONSTRAINT pk_cita PRIMARY KEY (id_cita),
    CONSTRAINT fk_medico_sucursal FOREIGN KEY (id_medico_sucursal) REFERENCES citamedica.medico_sucursal (id_medico_sucursal),
    CONSTRAINT fk_cita_usuario FOREIGN KEY (id_usuario) REFERENCES citamedica.usuario (id_usuario),
    CONSTRAINT fk_cita_estatus FOREIGN KEY (id_estatus_cita) REFERENCES citamedica.estatus_cita (id_estatus_cita),
    CONSTRAINT uk_cita_unique UNIQUE (id_usuario, id_medico_sucursal, fecha_hora_inicio)
);

ALTER SEQUENCE citamedica.seq_cita
    OWNED BY citamedica.cita.id_cita;