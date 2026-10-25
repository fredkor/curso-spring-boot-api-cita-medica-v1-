CREATE SEQUENCE ${securitySchema}.seq_permiso AS INTEGER START WITH 1 INCREMENT BY 1 MINVALUE 1 NO MAXVALUE CACHE 1 NO CYCLE;

CREATE TABLE ${securitySchema}.permiso (
    id_permiso INTEGER NOT NULL DEFAULT nextval('${securitySchema}.seq_permiso'::regclass),
    clave VARCHAR(50) NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_permiso PRIMARY KEY (id_permiso),
    CONSTRAINT uk_permiso_clave UNIQUE (clave)
);

ALTER SEQUENCE ${securitySchema}.seq_permiso OWNED BY ${securitySchema}.permiso.id_permiso;
