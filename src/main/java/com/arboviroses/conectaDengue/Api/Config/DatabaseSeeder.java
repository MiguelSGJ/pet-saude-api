package com.arboviroses.conectaDengue.Api.Config;

import com.arboviroses.conectaDengue.Domain.Services.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private final AuthenticationService authenticationService;

    @Override
    public void run(ApplicationArguments args) {
        authenticationService.seed();
    }
}
