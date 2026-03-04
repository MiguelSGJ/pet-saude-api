package com.arboviroses.conectaDengue.Domain.Repositories.Lira;

import com.arboviroses.conectaDengue.Domain.Entities.Lira.Lira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LiraRepository extends JpaRepository<Lira, Long> {
    List<Lira> findByAno(Integer ano);
    List<Lira> findByBairroAndAno(String bairro, Integer ano);
    List<Lira> findByAnoAndLiraNumber(Integer ano, Integer liraNumber);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Lira l WHERE l.ano = :ano AND l.liraNumber = :liraNumber")
    void deleteByAnoAndLiraNumber(@Param("ano") Integer ano, @Param("liraNumber") Integer liraNumber);
}
