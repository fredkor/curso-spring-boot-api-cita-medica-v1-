INSERT INTO citamedica.usuario (
    username,
    password,
    nombres,
    apellido_paterno,
    apellido_materno,
    correo,
    fecha_creacion,
    activo
)
VALUES (
           'admin',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Administrador',
           'Sistema',
           NULL,
           'admin@citamedica.fredkor.com.mx',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 1, id_rol, CURRENT_TIMESTAMP FROM rol;
