package com.arboviroses.conectaDengue.Domain.Services.Bairros;

import com.arboviroses.conectaDengue.Api.DTO.BairroSeedItem;
import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import com.arboviroses.conectaDengue.Domain.Entities.SubBairro;
import com.arboviroses.conectaDengue.Domain.Repositories.Bairros.BairroRepository;
import com.arboviroses.conectaDengue.Utils.Search.SearchAlgorithms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BairroService {
    private static final String DEFAULT_CITY = "MOSSORO";
    private final BairroRepository bairroRepository;

    // Encontra o bairro pelo nome do sub-bairro
    public Optional<Bairro> findBySubBairro(String subBairroNome) {
        String nomeNormalizado = normalizeName(subBairroNome);
        if (nomeNormalizado == null) {
            return Optional.empty();
        }

        List<Bairro> bairros = bairroRepository.findAllBySubBairroNome(nomeNormalizado);
        if (!bairros.isEmpty()) {
            return Optional.of(bairros.get(0));
        }

        return bairroRepository.findByNomeAndCidadeIgnoreCase(nomeNormalizado, DEFAULT_CITY);
    }

    // Normaliza os nomes dos bairros/sub-bairros, esse metodo aplica um algoritmo para encontrar
    // o sub-bairro com o nome mais proximo ao que veio na entrada. Ex: SNTA DELMRA -> SANTA DELMIRA
    public String normalizeToMainNeighborhood(String nomeBairroOuSubBairro) {
        Optional<Bairro> exactMatch = findBySubBairro(nomeBairroOuSubBairro);
        if (exactMatch.isPresent()) {
            return exactMatch.get().getNome();
        }

        return findClosestNeighborhood(nomeBairroOuSubBairro)
            .map(Bairro::getNome)
            .orElse(null);
    }

    public List<String> listNeighborhoodNames() {
        return bairroRepository.findAllNeighborhoodNames();
    }

    @Transactional // Carrega bairros em lote para salvar no banco, caso ocorra um erro da um rollback
    public void loadNeighborhoodCatalog(List<BairroSeedItem> bairrosParaCarga) {
        if (bairrosParaCarga == null || bairrosParaCarga.isEmpty()) {
            return;
        }

        bairrosParaCarga.forEach(item -> {
            String nomeBairro = normalizeName(item.getBairro());
            if (nomeBairro == null) {
                return;
            }

            String cidadeFinal = Optional.ofNullable(normalizeName(item.getCidade()))
                .orElse(DEFAULT_CITY);

            Bairro bairro = bairroRepository.findByNomeAndCidadeIgnoreCase(nomeBairro, cidadeFinal)
                .orElseGet(() -> {
                    Bairro novoBairro = new Bairro();
                    novoBairro.setNome(nomeBairro);
                    novoBairro.setCidade(cidadeFinal);
                    return novoBairro;
                });

            Set<String> subBairrosExistentes = bairro.getSubBairros().stream()
                .map(SubBairro::getNome)
                .filter(Objects::nonNull)
                .collect(HashSet::new, Set::add, Set::addAll);

            List<String> subBairros = item.getSubBairros() == null ? List.of() : item.getSubBairros();
            subBairros.stream()
                .map(this::normalizeName)
                .filter(Objects::nonNull)
                .filter(subBairrosExistentes::add)
                .forEach(subBairroNormalizado ->
                    bairro.addSubBairro(new SubBairro(null, subBairroNormalizado, bairro))
                );

            bairroRepository.save(bairro);
        });
    }

    // Normaliza os nomes, deixa em UPPER CASE, remove acentos
    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        normalized = normalized.replaceAll("\\s+", " ");
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private Optional<Bairro> findClosestNeighborhood(String name) {
        String normalizedInput = normalizeName(name);
        if (normalizedInput == null) {
            return Optional.empty();
        }

        List<Bairro> bairros = bairroRepository.findAllWithSubBairros();
        Bairro bestBairro = null;
        int bestDistance = Integer.MAX_VALUE;
        int bestCandidateLength = Integer.MAX_VALUE;

        for (Bairro bairro : bairros) {
            if (bairro.getNome() != null) {
                int distance = SearchAlgorithms.levenstein(normalizedInput, bairro.getNome());
                if (isBetterCandidate(distance, bairro.getNome().length(), bestDistance, bestCandidateLength)) {
                    bestDistance = distance;
                    bestCandidateLength = bairro.getNome().length();
                    bestBairro = bairro;
                }
            }

            for (SubBairro subBairro : bairro.getSubBairros()) {
                if (subBairro.getNome() == null) {
                    continue;
                }

                int distance = SearchAlgorithms.levenstein(normalizedInput, subBairro.getNome());
                if (isBetterCandidate(distance, subBairro.getNome().length(), bestDistance, bestCandidateLength)) {
                    bestDistance = distance;
                    bestCandidateLength = subBairro.getNome().length();
                    bestBairro = bairro;
                }
            }
        }

        if (bestBairro == null || !isDistanceAcceptable(normalizedInput.length(), bestDistance)) {
            return Optional.empty();
        }

        return Optional.of(bestBairro);
    }

    private boolean isBetterCandidate(int distance, int candidateLength, int bestDistance, int bestCandidateLength) {
        if (distance < bestDistance) {
            return true;
        }
        if (distance == bestDistance) {
            return candidateLength < bestCandidateLength;
        }
        return false;
    }

    private boolean isDistanceAcceptable(int inputLength, int distance) {
        if (inputLength <= 4) {
            return distance <= 1;
        }
        if (inputLength <= 8) {
            return distance <= 2;
        }
        if (inputLength <= 14) {
            return distance <= 3;
        }
        return distance <= 4;
    }

}
