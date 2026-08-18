-- Garantiza a nivel de base de datos que codigo_externo no se duplique.
-- Los 6 pares duplicados detectados fueron limpiados manualmente antes de
-- aplicar esta migración; a la fecha no existen códigos repetidos.
ALTER TABLE licitaciones
    ADD CONSTRAINT uk_licitaciones_codigo_externo UNIQUE (codigo_externo);
