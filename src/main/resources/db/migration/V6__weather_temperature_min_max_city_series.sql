ALTER TABLE weather_daily
    ADD COLUMN IF NOT EXISTS temperature_min_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS temperature_max_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS open_meteo_temperature_min_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS open_meteo_temperature_max_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS nasa_power_temperature_min_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS nasa_power_temperature_max_c DOUBLE PRECISION;

-- Serie diaria da cidade: media simples dos bairros em cada dia.
-- Os contadores null_* mostram quantos bairros ficaram sem valor (nada e interpolado).
CREATE OR REPLACE VIEW weather_daily_city AS
SELECT
    weather_date,
    COUNT(*) AS n_bairros,
    ROUND(AVG(temperature_c)::numeric, 3)::double precision AS temperature_c,
    ROUND(AVG(temperature_min_c)::numeric, 3)::double precision AS temperature_min_c,
    ROUND(AVG(temperature_max_c)::numeric, 3)::double precision AS temperature_max_c,
    ROUND(AVG(precipitation_mm)::numeric, 3)::double precision AS precipitation_mm,
    ROUND(AVG(relative_humidity_pct)::numeric, 3)::double precision AS relative_humidity_pct,
    ROUND(AVG(wind_speed_kmh)::numeric, 3)::double precision AS wind_speed_kmh,
    COUNT(*) - COUNT(temperature_c) AS null_temperature,
    COUNT(*) - COUNT(temperature_min_c) AS null_temperature_min,
    COUNT(*) - COUNT(temperature_max_c) AS null_temperature_max,
    COUNT(*) - COUNT(precipitation_mm) AS null_precipitation,
    COUNT(*) - COUNT(relative_humidity_pct) AS null_humidity,
    COUNT(*) - COUNT(wind_speed_kmh) AS null_wind
FROM weather_daily
GROUP BY weather_date;
