package com.arboviroses.conectaDengue.Domain.Repositories.Users;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.arboviroses.conectaDengue.Domain.Entities.RefreshToken;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);   
    Optional<RefreshToken> findByUserId(long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken n WHERE n.user.id = :userId")
    void deleteByUserId(long userId);
}