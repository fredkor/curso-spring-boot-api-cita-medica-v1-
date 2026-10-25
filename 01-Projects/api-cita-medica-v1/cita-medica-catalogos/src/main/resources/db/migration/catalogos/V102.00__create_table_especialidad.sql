CREATE SEQUENCE citamedica.seq_especialidad
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    NO CYCLE;

CREATE TABLE citamedica.especialidad (
    id_especialidad INTEGER NOT NULL DEFAULT nextval('citamedica.seq_especialidad'::regclass),
    nombre character varying(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_especialidad PRIMARY KEY (id_especialidad),
    CONSTRAINT uk_especialidad_nombre UNIQUE (nombre)
);

ALTER SEQUENCE citamedica.seq_especialidad
    OWNED BY citamedica.especialidad.id_especialidad;