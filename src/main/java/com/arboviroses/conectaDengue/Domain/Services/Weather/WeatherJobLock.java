package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WeatherJobLock {
    private final JdbcTemplate jdbcTemplate;
    private final WeatherProperties properties;
    private final String owner = UUID.randomUUID().toString();
    private final AtomicBoolean localLock = new AtomicBoolean(false);

    public WeatherJobLock(JdbcTemplate jdbcTemplate, WeatherProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public boolean tryAcquire() {
        if (!localLock.compareAndSet(false, true)) {
            return false;
        }
        try {
            int updated = jdbcTemplate.update("""
                UPDATE weather_job_lock
                SET locked_until = CURRENT_TIMESTAMP + (? * INTERVAL '1 second'),
                    locked_by = ?
                WHERE id = 1
                  AND locked_until < CURRENT_TIMESTAMP
                """, properties.getJobLockSeconds(), owner);
            if (updated == 0) {
                localLock.set(false);
                return false;
            }
            return true;
        } catch (RuntimeException exception) {
            localLock.set(false);
            throw exception;
        }
    }

    public void release() {
        try {
            jdbcTemplate.update("""
                UPDATE weather_job_lock
                SET locked_until = TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00',
                    locked_by = NULL
                WHERE id = 1 AND locked_by = ?
                """, owner);
        } finally {
            localLock.set(false);
        }
    }
}

