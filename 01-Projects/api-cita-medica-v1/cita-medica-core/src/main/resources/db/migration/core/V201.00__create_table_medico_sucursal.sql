CREATE SEQUENCE citamedica.seq_medico_sucursal
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    NO CYCLE;

CREATE TABLE citamedica.medico_sucursal (
    id_medico_sucursal INTEGER NOT NULL DEFAULT nextval('citamedica.seq_medico_sucursal'::regclass),
    id_medico_especialidad integer NOT NULL,
    id_sucursal integer NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_medico_sucursal PRIMARY KEY (id_medico_sucursal),
    CONSTRAINT fk_medico_sucursal_especialidad FOREIGN KEY (id_medico_especialidad) REFERENCES citamedica.medico_especialidad (id_medico_especialidad),
    CONSTRAINT fk_medico_sucursal_sucursal FOREIGN KEY (id_sucursal) REFERENCES citamedica.sucursal (id_sucursal),
    CONSTRAINT uk_medico_sucursal_unique UNIQUE (id_medico_especialidad, id_sucursal)
);

ALTER SEQUENCE citamedica.seq_medico_sucursal
    OWNED BY citamedica.medico_sucursal.id_medico_sucursal;