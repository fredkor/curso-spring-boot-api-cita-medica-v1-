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
           'emiliano.vh',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'José Emiliano Guadalupe',
           'Victoria',
           null,
           'emiliano.vh@citamedica.fredkor.com.mx',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 2, id_rol, CURRENT_TIMESTAMP FROM rol
where id_rol != 1;

-- Se insertan las especialidades
INSERT INTO medico_especialidad (id_usuario, id_especialidad, fecha_creacion)
SELECT 2, id_especialidad, CURRENT_TIMESTAMP FROM especialidad
where id_especialidad in (1,2,3,4,5,6,7,8);

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
           'maria.jr',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Maria Matías',
           'Juárez',
           null,
           'maria.jr@citamedica.fredkor.com.mx',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 3, id_rol, CURRENT_TIMESTAMP FROM rol
where id_rol != 1;

-- Se insertan las especialidades
INSERT INTO medico_especialidad (id_usuario, id_especialidad, fecha_creacion)
SELECT 3, id_especialidad, CURRENT_TIMESTAMP FROM especialidad
where id_especialidad in (9,10,11,12,13,14,15,16);

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
           'cinthya.cm',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Cinthya',
           'Córdoba',
           'Manilla',
           'cinthya.cm@citamedica.fredkor.com.mx',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 4, id_rol, CURRENT_TIMESTAMP FROM rol
where id_rol != 1;


-- Se insertan las especialidades
INSERT INTO medico_especialidad (id_usuario, id_especialidad, fecha_creacion)
SELECT 4, id_especialidad, CURRENT_TIMESTAMP FROM especialidad
where id_especialidad in (17,18,19,20);

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
           'samantha.ch',
           '$2a$10$e6Ijw.dlWTn8KkEsbF3w9uDgNGFSz/gXV3BVrR/LBElZOmOAtRL4S',
           'Samantha',
           'Cortes',
           null,
           'samantha.ch@citamedica.fredkor.com.mx',
           CURRENT_TIMESTAMP,
           TRUE
       );

-- Se asigna al admin todos los roles.
INSERT INTO usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT 5, id_rol, CURRENT_TIMESTAMP FROM rol
where id_rol != 1;

-- Se insertan las especialidades
INSERT INTO medico_especialidad (id_usuario, id_especialidad, fecha_creacion)
SELECT 5, id_especialidad, CURRENT_TIMESTAMP FROM especialidad
where id_especialidad in (1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20);