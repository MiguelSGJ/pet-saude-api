package com.arboviroses.conectaDengue.unit.services;

import com.arboviroses.conectaDengue.Domain.Repositories.Determinantes.DeterminantesRepository;
import com.arboviroses.conectaDengue.Domain.Services.Determinantes.SocialDeterminantsService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SocialDeterminantsServiceTest {

    @Test
    void shouldReturnDistinctNeighborhoodsWithoutBlankValues() {
        DeterminantesRepository repository = (DeterminantesRepository) Proxy.newProxyInstance(
            DeterminantesRepository.class.getClassLoader(),
            new Class<?>[] { DeterminantesRepository.class },
            (proxy, method, args) -> {
                if ("findDistinctNeighborhoods".equals(method.getName())) {
                    return Arrays.asList("SANTO ANTONIO", "", " ABOLICAO ", null, "BARROCAS", "ABOLICAO");
                }

                if ("toString".equals(method.getName())) {
                    return "DeterminantesRepositoryStub";
                }

                throw new UnsupportedOperationException("Metodo nao suportado no teste: " + method.getName());
            }
        );

        SocialDeterminantsService service = new SocialDeterminantsService(repository);

        assertThat(service.getAllNeighborhoods())
            .containsExactly("ABOLICAO", "BARROCAS", "SANTO ANTONIO");
    }
}
