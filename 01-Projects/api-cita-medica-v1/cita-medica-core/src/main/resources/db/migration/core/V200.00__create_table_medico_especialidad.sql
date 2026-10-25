CREATE SEQUENCE citamedica.seq_medico_especialidad
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    NO CYCLE;

CREATE TABLE citamedica.medico_especialidad (
    id_medico_especialidad INTEGER NOT NULL DEFAULT nextval('citamedica.seq_medico_especialidad'::regclass),
    id_usuario integer NOT NULL,
    id_especialidad integer NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_medico_especialidad PRIMARY KEY (id_medico_especialidad),
    CONSTRAINT fk_medico_especialidad_usuario FOREIGN KEY (id_usuario) REFERENCES citamedica.usuario (id_usuario),
    CONSTRAINT fk_medico_especialidad_especialidad FOREIGN KEY (id_especialidad) REFERENCES citamedica.especialidad (id_especialidad),
    CONSTRAINT uk_medico_especialidad_unique UNIQUE (id_usuario, id_especialidad)
);

ALTER SEQUENCE citamedica.seq_medico_especialidad
    OWNED BY citamedica.medico_especialidad.id_medico_especialidad;