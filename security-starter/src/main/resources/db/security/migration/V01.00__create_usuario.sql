CREATE SEQUENCE ${securitySchema}.seq_usuario AS INTEGER START WITH 1 INCREMENT BY 1 MINVALUE 1 NO MAXVALUE CACHE 1 NO CYCLE;

CREATE TABLE ${securitySchema}.usuario (
    id_usuario INTEGER NOT NULL DEFAULT nextval('${securitySchema}.seq_usuario'::regclass),
    username VARCHAR(50) NOT NULL,
    password VARCHAR(150) NOT NULL,
    nombres VARCHAR(150) NOT NULL,
    apellido_paterno VARCHAR(150) NOT NULL,
    apellido_materno VARCHAR(150),
    correo VARCHAR(150) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_correo UNIQUE (correo)
);

ALTER SEQUENCE ${securitySchema}.seq_usuario OWNED BY ${securitySchema}.usuario.id_usuario;
