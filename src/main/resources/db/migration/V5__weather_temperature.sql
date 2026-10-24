ALTER TABLE weather_daily
    ADD COLUMN IF NOT EXISTS temperature_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS temperature_source VARCHAR(40),
    ADD COLUMN IF NOT EXISTS open_meteo_temperature_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS nasa_power_temperature_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS temperature_provider_difference_c DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS temperature_divergent BOOLEAN;
