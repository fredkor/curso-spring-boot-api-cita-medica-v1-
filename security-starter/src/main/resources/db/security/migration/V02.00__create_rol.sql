CREATE SEQUENCE ${securitySchema}.seq_rol AS INTEGER START WITH 1 INCREMENT BY 1 MINVALUE 1 NO MAXVALUE CACHE 1 NO CYCLE;

CREATE TABLE ${securitySchema}.rol (
    id_rol INTEGER NOT NULL DEFAULT nextval('${securitySchema}.seq_rol'::regclass),
    clave VARCHAR(50) NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol),
    CONSTRAINT uk_rol_clave UNIQUE (clave)
);

ALTER SEQUENCE ${securitySchema}.seq_rol OWNED BY ${securitySchema}.rol.id_rol;
