package com.arboviroses.conectaDengue.Api.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arboviroses.conectaDengue.Api.DTO.request.LoginUserDTO;
import com.arboviroses.conectaDengue.Api.DTO.request.RefreshTokenRequestDTO;
import com.arboviroses.conectaDengue.Api.DTO.request.RegisterUserDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.JwtResponse;
import com.arboviroses.conectaDengue.Api.DTO.response.RegisterUser;
import com.arboviroses.conectaDengue.Api.Exceptions.PasswordNotMatchException;
import com.arboviroses.conectaDengue.Api.Exceptions.UserAlredyExistsException;
import com.arboviroses.conectaDengue.Domain.Entities.RefreshToken;
import com.arboviroses.conectaDengue.Domain.Entities.User;
import com.arboviroses.conectaDengue.Domain.Repositories.Users.UserRepository;
import com.arboviroses.conectaDengue.Domain.Services.AuthenticationService;
import com.arboviroses.conectaDengue.Domain.Services.JwtService;
import com.arboviroses.conectaDengue.Domain.Services.RefreshTokenService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;

@RequestMapping("api/auth")
@RestController
public class AuthenticationController {
    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private RefreshTokenService refreshTokenService;

    private final JwtService jwtService;
    
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUser> register(@Valid @RequestBody RegisterUserDTO registerUserDto) throws PasswordNotMatchException, UserAlredyExistsException {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(new RegisterUser(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticate(@RequestBody LoginUserDTO loginUserDto) throws UsernameNotFoundException {    
        Authentication authentication = authenticationService.authenticate(loginUserDto);

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        User authenticatedUser = userRepository.findByCpf(loginUserDto.getCpf()).get();

        refreshTokenService.deleteByUserId(authenticatedUser.getId());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginUserDto.getCpf());

        String jwtToken = jwtService.generateToken(authenticatedUser);

        JwtResponse loginResponse = new JwtResponse(jwtToken, refreshToken.getToken(), authenticatedUser.getFullName());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequest) throws UsernameNotFoundException {        
        JwtResponse response = refreshTokenService.findByToken(refreshTokenRequest.getToken())
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                    String accessToken = jwtService.generateToken(user);

                    return JwtResponse.builder()
                                        .jwtToken(accessToken)
                                        .token(refreshTokenRequest.getToken()).build();
                    }).orElseThrow(() -> new ExpiredJwtException(null, null, "Refresh Token não é válido!"));

        return ResponseEntity.ok(response);
    }
}
