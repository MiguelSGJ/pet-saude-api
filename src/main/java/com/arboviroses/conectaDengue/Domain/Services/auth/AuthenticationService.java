package com.arboviroses.conectaDengue.Domain.Services.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arboviroses.conectaDengue.Api.DTO.request.LoginUserDTO;
import com.arboviroses.conectaDengue.Api.DTO.request.RegisterUserDTO;
import com.arboviroses.conectaDengue.Api.Exceptions.PasswordNotMatchException;
import com.arboviroses.conectaDengue.Api.Exceptions.UserAlredyExistsException;
import com.arboviroses.conectaDengue.Domain.Entities.User;
import com.arboviroses.conectaDengue.Domain.Entities.UserRole;
import com.arboviroses.conectaDengue.Domain.Repositories.Users.UserRepository;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${seed.user.name}")
    private String seedName;

    @Value("${seed.user.cpf}")
    private String seedCpf;

    @Value("${seed.user.password}")
    private String seedPassword;

    public AuthenticationService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDTO input) throws PasswordNotMatchException, UserAlredyExistsException {           
        if (!(input.getPassword().equals(input.getConfirmPassword()))) {
            throw new PasswordNotMatchException("Senhas não conferem");
        }

        User userExists = userRepository.findByCpf(input.getCpf()).orElse(null);

        if (userExists != null) {
            throw new UserAlredyExistsException("Usuario já cadastrado");
        }

        User user = new User()
                .setFullName(input.getFullName())
                .setCpf(input.getCpf())
                .setPassword(passwordEncoder.encode(input.getPassword()))
                .setRole(resolveRole(input.getRole(), UserRole.USER));

        return userRepository.save(user);
    }

    public Authentication authenticate(LoginUserDTO input) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getCpf(),
                        input.getPassword()
                )
        );

        return auth;
    }

    public void seed() {
        User existingUser = userRepository.findByCpf(seedCpf).orElse(null);

        if (existingUser != null) {
            if (existingUser.getRole() != UserRole.ADMIN) {
                existingUser.setRole(UserRole.ADMIN);
                userRepository.save(existingUser);
            }
            return;
        }

        User user = new User()
                .setFullName(seedName)
                .setCpf(seedCpf)
                .setPassword(passwordEncoder.encode(seedPassword))
                .setRole(UserRole.ADMIN);

        userRepository.save(user);
    }

    private UserRole resolveRole(String role, UserRole fallback) {
        if (role == null || role.isBlank()) {
            return fallback;
        }

        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
