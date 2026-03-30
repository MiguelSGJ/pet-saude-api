package com.arboviroses.conectaDengue.Domain.Repositories.Bairros;

import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BairroRepository extends JpaRepository<Bairro, Long> {

    Optional<Bairro> findByNomeAndCidadeIgnoreCase(String nome, String cidade);

    @Query("""
        SELECT DISTINCT b
        FROM Bairro b
        JOIN b.subBairros sb
        WHERE UPPER(sb.nome) = UPPER(:name)
    """)
    List<Bairro> findAllBySubBairroNome(@Param("name") String name);

    @Query("SELECT DISTINCT b.nome FROM Bairro b ORDER BY b.nome")
    List<String> findAllNeighborhoodNames();

    @Query("""
        SELECT DISTINCT b
        FROM Bairro b
        LEFT JOIN FETCH b.subBairros
    """)
    List<Bairro> findAllWithSubBairros();
}
