CREATE SEQUENCE ${securitySchema}.seq_usuario_rol AS INTEGER START WITH 1 INCREMENT BY 1 MINVALUE 1 NO MAXVALUE CACHE 1 NO CYCLE;

CREATE TABLE ${securitySchema}.usuario_rol (
    id_usuario_rol INTEGER NOT NULL DEFAULT nextval('${securitySchema}.seq_usuario_rol'::regclass),
    id_usuario INTEGER NOT NULL,
    id_rol INTEGER NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario_rol PRIMARY KEY (id_usuario_rol),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES ${securitySchema}.usuario (id_usuario),
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES ${securitySchema}.rol (id_rol),
    CONSTRAINT uk_usuario_rol UNIQUE (id_usuario, id_rol)
);

ALTER SEQUENCE ${securitySchema}.seq_usuario_rol OWNED BY ${securitySchema}.usuario_rol.id_usuario_rol;
