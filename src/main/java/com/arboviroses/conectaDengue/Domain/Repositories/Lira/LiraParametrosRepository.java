package com.arboviroses.conectaDengue.Domain.Repositories.Lira;

import com.arboviroses.conectaDengue.Domain.Entities.Lira.LiraParametros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiraParametrosRepository extends JpaRepository<LiraParametros, Long> {
    Optional<LiraParametros> findByAnoAndLiraNumber(Integer ano, Integer liraNumber);
    List<LiraParametros> findByAnoOrderByLiraNumberAsc(Integer ano);
}
