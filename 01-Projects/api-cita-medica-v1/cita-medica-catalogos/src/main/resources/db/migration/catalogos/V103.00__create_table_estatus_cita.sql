CREATE SEQUENCE citamedica.seq_estatus_cita
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    NO CYCLE;

CREATE TABLE citamedica.estatus_cita (
    id_estatus_cita INTEGER NOT NULL DEFAULT nextval('citamedica.seq_estatus_cita'::regclass),
    clave character varying(50) NOT NULL,
    descripcion character varying(100) NOT NULL,

    CONSTRAINT pk_estatus_cita PRIMARY KEY (id_estatus_cita),
    CONSTRAINT uk_estatus_cita_clave UNIQUE (clave)
);

ALTER SEQUENCE citamedica.seq_estatus_cita
    OWNED BY citamedica.estatus_cita.id_estatus_cita;