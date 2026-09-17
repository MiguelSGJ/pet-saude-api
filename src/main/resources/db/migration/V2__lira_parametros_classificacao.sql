CREATE TABLE IF NOT EXISTS lira_parametros (
    id BIGSERIAL PRIMARY KEY,
    ano INTEGER NOT NULL,
    lira_number INTEGER NOT NULL,
    limite_alerta DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    limite_risco DOUBLE PRECISION NOT NULL DEFAULT 4.0,
    CONSTRAINT uk_lira_parametros_ano_ciclo UNIQUE (ano, lira_number),
    CONSTRAINT ck_lira_parametros_limites CHECK (
        limite_alerta >= 0 AND limite_risco > limite_alerta
    )
);

INSERT INTO lira_parametros (ano, lira_number, limite_alerta, limite_risco)
SELECT DISTINCT ano, lira_number, 1.0, 4.0
FROM lira
WHERE lira_number IS NOT NULL
ON CONFLICT (ano, lira_number) DO NOTHING;
