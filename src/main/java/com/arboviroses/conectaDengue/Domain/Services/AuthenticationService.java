package com.arboviroses.conectaDengue.Domain.Services;

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
import com.arboviroses.conectaDengue.Domain.Repositories.Users.UserRepository;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final AuthenticationManager authenticationManager;

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
                .setPassword(passwordEncoder.encode(input.getPassword()));

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
}