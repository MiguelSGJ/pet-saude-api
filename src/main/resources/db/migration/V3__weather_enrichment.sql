ALTER TABLE bairros ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE bairros ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE bairros ADD COLUMN IF NOT EXISTS coordinates_source VARCHAR(120);
ALTER TABLE bairros ADD COLUMN IF NOT EXISTS coordinates_updated_at TIMESTAMP WITH TIME ZONE;

UPDATE bairros AS b
SET latitude = v.latitude,
    longitude = v.longitude,
    coordinates_source = 'OPENSTREETMAP_NOMINATIM_2026-10-20',
    coordinates_updated_at = CURRENT_TIMESTAMP
FROM (VALUES
    ('ABOLICOES', -5.1580539, -37.3606673),
    ('AEROPORTO', -5.1970112, -37.3695298),
    ('ALTO DE SAO MANOEL', -5.2101249, -37.3334201),
    ('ALTO DO SUMARE', -5.2331156, -37.3370185),
    ('ALTO DA CONCEICAO', -5.2004880, -37.3479802),
    ('ALTO DA BELA VISTA', -5.2125869, -37.3432887),
    ('ALAGADOS', -5.2153718, -37.3516995),
    ('BARROCAS', -5.1655503, -37.3322547),
    ('BOM JARDIM', -5.1791963, -37.3381264),
    ('BELO HORIZONTE', -5.2106005, -37.3574203),
    ('BOA VISTA', -5.1947398, -37.3535583),
    ('BOM JESUS', -5.2294878, -37.3614192),
    ('CENTRO', -5.1920530, -37.3413511),
    ('COSTA E SILVA', -5.2103888, -37.3143152),
    ('DOM JAIME CAMARA', -5.2284588, -37.3146408),
    ('DOZE ANOS', -5.1897814, -37.3507153),
    ('GOV DIX SEPT ROSADO', -5.2014882, -37.3649222),
    ('ITAPETINGA', -5.2122711, -37.3864290),
    ('ILHA DE SANTA LUZIA', -5.2001079, -37.3391123),
    ('LAGOA DO MATO', -5.2051842, -37.3600530),
    ('MONS ALFREDO SIMONNETI', -5.1844976, -37.3691797),
    ('NOVA BETANIA', -5.1787680, -37.3666830),
    ('PINTOS', -5.1797404, -37.3176846),
    ('PAREDOES', -5.1800072, -37.3305597),
    ('PLANALTO 13 DE MAIO', -5.2199344, -37.3384363),
    ('REDENCAO', -5.1423830, -37.3463139),
    ('RINCAO', -5.1970980, -37.2880033),
    ('SANTO ANTONIO', -5.1663384, -37.3432675),
    ('SANTA DELMIRA', -5.1523571, -37.3562337),
    ('SANTA JULIA', -5.1353757, -37.3281929)
) AS v(nome, latitude, longitude)
WHERE UPPER(b.nome) = v.nome
  AND UPPER(b.cidade) = 'MOSSORO'
  AND (b.latitude IS NULL OR b.longitude IS NULL);

CREATE TABLE weather_daily (
    id BIGSERIAL PRIMARY KEY,
    bairro_id BIGINT NOT NULL REFERENCES bairros(id),
    weather_date DATE NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    precipitation_mm DOUBLE PRECISION,
    relative_humidity_pct DOUBLE PRECISION,
    wind_speed_kmh DOUBLE PRECISION,
    precipitation_source VARCHAR(40),
    humidity_source VARCHAR(40),
    wind_source VARCHAR(40),
    open_meteo_precipitation_mm DOUBLE PRECISION,
    open_meteo_relative_humidity_pct DOUBLE PRECISION,
    open_meteo_wind_speed_kmh DOUBLE PRECISION,
    nasa_power_precipitation_mm DOUBLE PRECISION,
    nasa_power_relative_humidity_pct DOUBLE PRECISION,
    nasa_power_wind_speed_kmh DOUBLE PRECISION,
    open_meteo_fetched_at TIMESTAMP WITH TIME ZONE,
    nasa_power_fetched_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    last_error VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_weather_daily_bairro_date UNIQUE (bairro_id, weather_date),
    CONSTRAINT ck_weather_daily_humidity CHECK (relative_humidity_pct IS NULL OR relative_humidity_pct BETWEEN 0 AND 100),
    CONSTRAINT ck_weather_daily_precipitation CHECK (precipitation_mm IS NULL OR precipitation_mm >= 0),
    CONSTRAINT ck_weather_daily_wind CHECK (wind_speed_kmh IS NULL OR wind_speed_kmh >= 0)
);

CREATE INDEX idx_weather_daily_date ON weather_daily(weather_date);
CREATE INDEX idx_weather_daily_status ON weather_daily(status);

CREATE TABLE weather_job_lock (
    id SMALLINT PRIMARY KEY,
    locked_until TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_by VARCHAR(120)
);

INSERT INTO weather_job_lock(id, locked_until, locked_by)
VALUES (1, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00', NULL);

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS weather_data_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_notifications_weather_data'
    ) THEN
        ALTER TABLE notifications
            ADD CONSTRAINT fk_notifications_weather_data
            FOREIGN KEY (weather_data_id) REFERENCES weather_daily(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_notifications_weather_data_id ON notifications(weather_data_id);
CREATE INDEX IF NOT EXISTS idx_notifications_weather_lookup
    ON notifications(id_bairro, data_primeiro_sintoma, data_notification);

