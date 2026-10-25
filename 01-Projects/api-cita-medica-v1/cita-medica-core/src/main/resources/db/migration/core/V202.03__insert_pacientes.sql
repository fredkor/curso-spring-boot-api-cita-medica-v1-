-----------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------
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
           'yanelli.vs',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Mirian',
           'León',
           null,
           'yanelli.vs@google.com',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 6, id_rol, CURRENT_TIMESTAMP FROM rol where id_rol = 3;

-----------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------

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
           'aldo.vj',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Aldo',
           'Valente',
           null,
           'aldo.vj@google.com',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 7, id_rol, CURRENT_TIMESTAMP FROM rol where id_rol = 3;

-----------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------

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
           'diana.vj',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Cinthya',
           'Diana',
           'Vazqués',
           'diana.vj@citamedica.fredkor.com.mx',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 8, id_rol, CURRENT_TIMESTAMP FROM rol where id_rol = 3;

-----------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------

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
           'araceli.pn',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Araceli',
           'Pérez',
           'Nieto',
           'araceli.pn@google.mx',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 9, id_rol, CURRENT_TIMESTAMP FROM rol where id_rol = 3;
