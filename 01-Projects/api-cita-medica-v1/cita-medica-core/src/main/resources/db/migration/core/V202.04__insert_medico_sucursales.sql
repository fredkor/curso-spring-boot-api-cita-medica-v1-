
INSERT INTO medico_sucursal (id_sucursal, id_medico_especialidad, fecha_creacion)
SELECT id_sucursal, id_medico_especialidad, CURRENT_TIMESTAMP FROM medico_especialidad, sucursal
where id_sucursal <4;

INSERT INTO medico_sucursal (id_sucursal, id_medico_especialidad, fecha_creacion)
SELECT id_sucursal, id_medico_especialidad, CURRENT_TIMESTAMP FROM medico_especialidad, sucursal
where id_sucursal >=4 and id_sucursal <8;

INSERT INTO medico_sucursal (id_sucursal, id_medico_especialidad, fecha_creacion)
SELECT id_sucursal, id_medico_especialidad, CURRENT_TIMESTAMP FROM medico_especialidad, sucursal
where id_sucursal >=8 and id_sucursal <12;

INSERT INTO medico_sucursal (id_sucursal, id_medico_especialidad, fecha_creacion)
SELECT id_sucursal, id_medico_especialidad, CURRENT_TIMESTAMP FROM medico_especialidad, sucursal
where id_sucursal >12;

