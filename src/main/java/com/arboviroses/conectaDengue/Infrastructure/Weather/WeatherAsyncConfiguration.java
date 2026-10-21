package com.arboviroses.conectaDengue.Infrastructure.Weather;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class WeatherAsyncConfiguration {
    @Bean(name = "weatherTaskExecutor")
    public Executor weatherTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(2);
        executor.setThreadNamePrefix("weather-enrichment-");
        executor.initialize();
        return executor;
    }
}

