package com.arboviroses.conectaDengue.unit.services;

import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import com.arboviroses.conectaDengue.Domain.Repositories.Bairros.BairroRepository;
import com.arboviroses.conectaDengue.Domain.Services.Bairros.BairroService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BairroServiceTest {

    @Test
    void shouldClassifyUnknownFilledLocalityAsRuralZone() {
        BairroRepository repository = mock(BairroRepository.class);
        Bairro rural = new Bairro();
        rural.setId(31L);
        rural.setNome(BairroService.RURAL_NEIGHBORHOOD);
        rural.setCidade("MOSSORO");

        when(repository.findAllBySubBairroNome("SITIO JATOBA")).thenReturn(List.of());
        when(repository.findByNomeAndCidadeIgnoreCase("SITIO JATOBA", "MOSSORO")).thenReturn(Optional.empty());
        when(repository.findAllWithSubBairros()).thenReturn(List.of());
        when(repository.findByNomeAndCidadeIgnoreCase(BairroService.RURAL_NEIGHBORHOOD, "MOSSORO"))
            .thenReturn(Optional.of(rural));

        BairroService service = new BairroService(repository);

        assertThat(service.resolveBairroForNotification("Sítio Jatobá"))
            .contains(rural);
    }

    @Test
    void shouldKeepMissingNeighborhoodAsInconsistency() {
        BairroRepository repository = mock(BairroRepository.class);
        BairroService service = new BairroService(repository);

        assertThat(service.resolveBairroForNotification("  ")).isEmpty();
        verifyNoInteractions(repository);
    }
}
