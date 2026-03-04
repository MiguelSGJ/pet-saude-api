package com.arboviroses.conectaDengue.Domain.Services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.arboviroses.conectaDengue.Domain.Entities.RefreshToken;
import com.arboviroses.conectaDengue.Domain.Repositories.Users.RefreshTokenRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Users.UserRepository;

@Service
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${security.jwt.refreshToken.expiration-time}")
    private long jwtExpiration;

    public RefreshToken createRefreshToken(String cpf) 
    {
        RefreshToken refreshToken = RefreshToken.builder() 
                                    .user(userRepository.findByCpf(cpf).get())
                                    .token(UUID.randomUUID().toString())
                                    .expiryDate(Instant.now().plusMillis(jwtExpiration))
                                    .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public Optional<RefreshToken> findByUserId(long userId) 
    {
        return refreshTokenRepository.findByUserId(userId);
    }

    public void deleteByUserId(long userId)
    {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + "Refresh token expirado. faça um novo login!");
        }

        return token;
    }


}
