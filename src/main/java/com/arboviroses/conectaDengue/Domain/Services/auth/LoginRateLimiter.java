package com.arboviroses.conectaDengue.Domain.Services.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.arboviroses.conectaDengue.Api.Exceptions.ValidationException;

@Service
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Clock clock;
    private final Map<String, AttemptState> attemptsByKey = new ConcurrentHashMap<>();

    public LoginRateLimiter() {
        this(Clock.systemUTC());
    }

    LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void assertAllowed(String key) {
        AttemptState state = attemptsByKey.get(key);
        Instant now = clock.instant();

        if (state == null) {
            return;
        }

        if (state.lockedUntil != null && state.lockedUntil.isAfter(now)) {
            throw new ValidationException("Muitas tentativas de login. Aguarde alguns minutos e tente novamente.");
        }

        if (state.firstAttemptAt.plus(WINDOW).isBefore(now)) {
            attemptsByKey.remove(key);
        }
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        attemptsByKey.compute(key, (ignored, current) -> {
            AttemptState state = current;
            if (state == null || state.firstAttemptAt.plus(WINDOW).isBefore(now)) {
                state = new AttemptState(now);
            }

            state.failedAttempts++;
            if (state.failedAttempts >= MAX_ATTEMPTS) {
                state.lockedUntil = now.plus(LOCK_DURATION);
            }

            return state;
        });
    }

    public void recordSuccess(String key) {
        attemptsByKey.remove(key);
    }

    private static class AttemptState {
        private final Instant firstAttemptAt;
        private int failedAttempts;
        private Instant lockedUntil;

        private AttemptState(Instant firstAttemptAt) {
            this.firstAttemptAt = firstAttemptAt;
        }
    }
}
