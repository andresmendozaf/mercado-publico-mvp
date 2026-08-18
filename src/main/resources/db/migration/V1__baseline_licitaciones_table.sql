-- Baseline: reproduce la tabla licitaciones tal como existe hoy en producción
-- (creada originalmente por Hibernate vía ddl-auto=update).
--
-- En la base de datos actual (ya poblada) esta versión se marca como aplicada
-- mediante spring.flyway.baseline-on-migrate/baseline-version, sin ejecutarse.
-- En una base de datos nueva/vacía, Flyway sí la ejecuta, dejando la tabla
-- lista para que V2 agregue la restricción UNIQUE.
CREATE TABLE IF NOT EXISTS licitaciones (
    id                    BIGSERIAL PRIMARY KEY,
    codigo_externo        VARCHAR(255),
    descripcion           TEXT,
    estado                VARCHAR(255) NOT NULL
        CHECK (estado IN ('PUBLICADA', 'CERRADA', 'DESIERTA', 'ADJUDICADA', 'REVOCADA', 'ADJUDICADA_ART3', 'SUSPENDIDA', 'OTRO')),
    fecha_actualizacion   TIMESTAMP(6) WITH TIME ZONE,
    fecha_cierre          TIMESTAMP(6) WITH TIME ZONE,
    fecha_creacion        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    nombre                TEXT,
    organismo_comprador   VARCHAR(255),
    presupuesto_estimado  DOUBLE PRECISION,
    rut_comprador         VARCHAR(255)
);
