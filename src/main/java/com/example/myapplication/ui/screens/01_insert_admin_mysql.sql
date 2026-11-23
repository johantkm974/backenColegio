-- ============================================================
-- Script: Insertar administrador en reporta_db
-- Motor: MySQL / MariaDB
-- Fecha: 22/11/2026
-- ============================================================

USE reporta_db;

INSERT INTO usuarios (
    rol_id,
    nombres,
    apellidos,
    dni,
    correo,
    password_hash,
    telefono,
    activo,
    creado_en,
    actualizado_en
) VALUES (
    1,
    'Jorge Joseph',
    'Aguilar Apolinario',
    '42109090',
    'jjaa_mus@hotmail.com',
    '$2b$12$So9rOTJ5ohPlYvMVUzWO3.yOK3nyYiLVEU8eqKPf746Yi2.YUY4gW',
    NULL,
    1,
    NOW(),
    NOW()
);

-- Verificación
SELECT 
    id,
    nombres,
    apellidos,
    dni,
    correo,
    rol_id,
    activo,
    creado_en
FROM usuarios
WHERE dni = '42109090';