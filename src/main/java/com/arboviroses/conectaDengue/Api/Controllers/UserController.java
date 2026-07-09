package com.arboviroses.conectaDengue.Api.Controllers;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.arboviroses.conectaDengue.Api.DTO.request.UpdateProfileDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.JwtResponse;
import com.arboviroses.conectaDengue.Api.DTO.response.RegisterUser;
import com.arboviroses.conectaDengue.Api.DTO.response.UserProfileResponse;
import com.arboviroses.conectaDengue.Domain.Entities.User;
import com.arboviroses.conectaDengue.Domain.Repositories.Users.UserRepository;
import com.arboviroses.conectaDengue.Domain.Services.auth.JwtService;
import com.arboviroses.conectaDengue.Domain.Services.auth.RefreshTokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/user")
public class UserController {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserController(
        UserRepository userRepository,
        RefreshTokenService refreshTokenService,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(new UserProfileResponse(currentUser));
    }

    @GetMapping("/manage")
    public ResponseEntity<List<RegisterUser>> listUsers() {
        List<RegisterUser> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"))
            .stream()
            .map(RegisterUser::new)
            .toList();

        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        User targetUser = userRepository.findById(Long.valueOf(id))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e permitido apagar o proprio login.");
        }

        refreshTokenService.deleteByUserId(targetUser.getId());
        userRepository.delete(targetUser);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    public ResponseEntity<JwtResponse> updateProfile(@Valid @RequestBody UpdateProfileDTO input) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        userRepository.findByCpf(input.getCpf())
            .filter(existingUser -> !existingUser.getId().equals(currentUser.getId()))
            .ifPresent(existingUser -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ja existe um usuario com esse CPF.");
            });

        String nextPassword = normalizeOptional(input.getNewPassword());
        String confirmNextPassword = normalizeOptional(input.getConfirmNewPassword());
        String currentPassword = normalizeOptional(input.getCurrentPassword());

        boolean wantsToChangePassword = nextPassword != null || confirmNextPassword != null || currentPassword != null;

        if (wantsToChangePassword) {
            if (currentPassword == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a senha atual para alterar a senha.");
            }

            if (nextPassword == null || confirmNextPassword == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha a nova senha e a confirmacao.");
            }

            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
            }

            if (!nextPassword.equals(confirmNextPassword)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A confirmacao da nova senha nao confere.");
            }

            currentUser.setPassword(passwordEncoder.encode(nextPassword));
        }

        currentUser.setFullName(input.getFullName());
        currentUser.setCpf(input.getCpf());

        User savedUser = userRepository.save(currentUser);
        String jwtToken = jwtService.generateToken(savedUser);

        return ResponseEntity.ok(
            JwtResponse.builder()
                .jwtToken(jwtToken)
                .fullName(savedUser.getFullName())
                .cpf(savedUser.getCpf())
                .role(savedUser.getRole().name())
                .build()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }
}
