DO $$
BEGIN
    IF TO_REGCLASS('public.notifications') IS NOT NULL THEN
        ALTER TABLE notifications
            ADD COLUMN IF NOT EXISTS nome_bairro_original VARCHAR(120);
    END IF;

    IF TO_REGCLASS('public.notifications_with_error') IS NOT NULL THEN
        ALTER TABLE notifications_with_error
            ADD COLUMN IF NOT EXISTS nome_bairro_original VARCHAR(120);
    END IF;

    IF TO_REGCLASS('public.bairros') IS NOT NULL THEN
        INSERT INTO bairros (nome, cidade)
        SELECT 'ZONA RURAL', 'MOSSORO'
        WHERE NOT EXISTS (
            SELECT 1
            FROM bairros
            WHERE UPPER(nome) = 'ZONA RURAL'
              AND UPPER(cidade) = 'MOSSORO'
        );
    END IF;
END $$;
