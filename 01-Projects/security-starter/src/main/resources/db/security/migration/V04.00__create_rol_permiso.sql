CREATE SEQUENCE ${securitySchema}.seq_rol_permiso AS INTEGER START WITH 1 INCREMENT BY 1 MINVALUE 1 NO MAXVALUE CACHE 1 NO CYCLE;

CREATE TABLE ${securitySchema}.rol_permiso (
    id_rol_permiso INTEGER NOT NULL DEFAULT nextval('${securitySchema}.seq_rol_permiso'::regclass),
    id_rol INTEGER NOT NULL,
    id_permiso INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_rol_permiso PRIMARY KEY (id_rol_permiso),
    CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (id_rol) REFERENCES ${securitySchema}.rol (id_rol),
    CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (id_permiso) REFERENCES ${securitySchema}.permiso (id_permiso),
    CONSTRAINT uk_rol_permiso UNIQUE (id_rol, id_permiso)
);

ALTER SEQUENCE ${securitySchema}.seq_rol_permiso OWNED BY ${securitySchema}.rol_permiso.id_rol_permiso;
