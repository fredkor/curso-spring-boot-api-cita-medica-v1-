CREATE SEQUENCE citamedica.seq_sucursal
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    NO CYCLE;

CREATE TABLE citamedica.sucursal (
    id_sucursal INTEGER NOT NULL DEFAULT nextval('citamedica.seq_sucursal'::regclass),
    nombre character varying(100) NOT NULL,
    direccion character varying(500) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_sucursal PRIMARY KEY (id_sucursal),
    CONSTRAINT uk_sucursal_nombre UNIQUE (nombre)
);

ALTER SEQUENCE citamedica.seq_sucursal
    OWNED BY citamedica.sucursal.id_sucursal;