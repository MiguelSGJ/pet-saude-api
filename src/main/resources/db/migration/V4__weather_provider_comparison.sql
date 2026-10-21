ALTER TABLE weather_daily
    ADD COLUMN IF NOT EXISTS precipitation_provider_difference_mm DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS humidity_provider_difference_pct_points DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS wind_provider_difference_kmh DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS precipitation_divergent BOOLEAN,
    ADD COLUMN IF NOT EXISTS humidity_divergent BOOLEAN,
    ADD COLUMN IF NOT EXISTS wind_divergent BOOLEAN,
    ADD COLUMN IF NOT EXISTS comparison_status VARCHAR(30) NOT NULL DEFAULT 'UNAVAILABLE';

CREATE INDEX IF NOT EXISTS idx_weather_daily_comparison_status
    ON weather_daily(comparison_status);
