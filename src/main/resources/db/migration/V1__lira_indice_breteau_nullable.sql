-- O campo Lira.indiceBreteau era `double` primitivo e virou `Double` (nullable) para suportar
-- ciclo sem amostragem (celula vazia = NULL, nunca 0). O ddl-auto=update do Hibernate nao
-- remove constraints ja existentes, entao qualquer banco que ja tinha a tabela `lira` continua
-- rejeitando NULL. Esta migration corrige isso automaticamente em qualquer ambiente.
--
-- Guardado com EXISTS: se a tabela ainda nao existe (banco novo, Flyway roda antes do Hibernate
-- criar o schema), simplesmente nao faz nada - o Hibernate ja cria a coluna nullable de cara.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'lira'
          AND column_name = 'indice_breteau'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE lira ALTER COLUMN indice_breteau DROP NOT NULL;
    END IF;
END $$;
