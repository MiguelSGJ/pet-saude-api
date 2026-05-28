package com.arboviroses.conectaDengue.Domain.Filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

import com.arboviroses.conectaDengue.Api.DTO.response.AgravoCountBySemanaEpidemiologica;
import com.arboviroses.conectaDengue.Api.DTO.response.BairroCountDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.CountAgravoBySexoDTO;
import com.arboviroses.conectaDengue.Api.Exceptions.InvalidAgravoException;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Utils.ConvertNameToIdAgravo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;

public class NotificationFilters {
    private static final List<String> CONFIRMED_CLASSIFICATIONS = Arrays.asList("10", "11", "12");

    @PersistenceContext
    private EntityManager entityManager;

    public static Specification<Notification> buildDashboardSpecification(HttpServletRequest request) throws InvalidAgravoException {
        String agravoName = request.getParameter("agravo");
        String bairro = request.getParameter("bairro");
        Integer year = request.getParameter("year") != null ? Integer.valueOf(request.getParameter("year")) : null;
        String agravoId = resolveAgravoId(agravoName);
        Optional<List<String>> classificacaoFilter = resolveClassificacaoFilter(agravoName);
        String scope = normalizeScope(request.getParameter("scope"));

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (agravoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("idAgravo"), agravoId));
            }
            classificacaoFilter.ifPresent(classes ->
                predicates.add(root.get("classificacao").in(classes))
            );
            if (bairro != null && !bairro.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("nomeBairro"), bairro));
            }
            if (year != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.function("date_part", Integer.class,
                    criteriaBuilder.literal("year"),
                    root.<java.util.Date>get("dataPrimeiroSintoma")), year));
            }

            applyScope(scope, predicates, root, criteriaBuilder);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static CountAgravoBySexoDTO filtersForNotificationsInfoBySexo(HttpServletRequest request, NotificationRepository notificationRepository) throws InvalidAgravoException {
        Specification<Notification> baseSpec = buildDashboardSpecification(request);

        Specification<Notification> specMasculino = baseSpec.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("sexo"), "M")
        );

        Specification<Notification> specFeminino = baseSpec.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("sexo"), "F")
        );
        
        return new CountAgravoBySexoDTO(
            notificationRepository.count(specMasculino),
            notificationRepository.count(specFeminino)
        );
    }

    public static List<AgravoCountBySemanaEpidemiologica> filtersForNotificationsInfoBySemanaEpidemiologica(HttpServletRequest request, NotificationRepository notificationRepository) throws InvalidAgravoException {
        Specification<Notification> baseSpec = buildDashboardSpecification(request);
        Integer semanaInicial = request.getParameter("semanaInicial") != null ? Integer.valueOf(request.getParameter("semanaInicial")) : null;
        Integer semanaFinal = request.getParameter("semanaFinal") != null ? Integer.valueOf(request.getParameter("semanaFinal")) : null;

        Specification<Notification> rangeSpec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (semanaInicial != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("semanaEpidemiologica"), semanaInicial));
            }
            if (semanaFinal != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("semanaEpidemiologica"), semanaFinal));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Specification<Notification> spec = baseSpec.and(rangeSpec);
        return notificationRepository.buscarContagemPorSemanaEpidemiologica(spec);
    }

    public static Map<String, Integer> filtersForNotificationsByAgeRange(HttpServletRequest request, EntityManager entityManager) throws InvalidAgravoException {
        Integer year = request.getParameter("year") != null && !request.getParameter("year").isEmpty()
                ? Integer.valueOf(request.getParameter("year"))
                : null;
        String agravoName = request.getParameter("agravo");
        String bairro = request.getParameter("bairro");
        String agravoId = resolveAgravoId(agravoName);
        String scope = normalizeScope(request.getParameter("scope"));

        List<String> ageBrackets = List.of(
            "age0to1", "age2to3", "age4to5", "age6to7", "age8to9",
            "age10to19", "age20to29", "age30to39", "age40to49", "age50to59",
            "age60to69", "age70to79", "age80to89", "age90to99"
        );

        StringBuilder jpql = new StringBuilder();
        jpql.append("""
            SELECT
                SUM(CASE WHEN n.idadePaciente BETWEEN 0 AND 1 THEN 1 ELSE 0 END) AS age0to1,
                SUM(CASE WHEN n.idadePaciente BETWEEN 2 AND 3 THEN 1 ELSE 0 END) AS age2to3,
                SUM(CASE WHEN n.idadePaciente BETWEEN 4 AND 5 THEN 1 ELSE 0 END) AS age4to5,
                SUM(CASE WHEN n.idadePaciente BETWEEN 6 AND 7 THEN 1 ELSE 0 END) AS age6to7,
                SUM(CASE WHEN n.idadePaciente BETWEEN 8 AND 9 THEN 1 ELSE 0 END) AS age8to9,
                SUM(CASE WHEN n.idadePaciente BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS age10to19,
                SUM(CASE WHEN n.idadePaciente BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS age20to29,
                SUM(CASE WHEN n.idadePaciente BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS age30to39,
                SUM(CASE WHEN n.idadePaciente BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS age40to49,
                SUM(CASE WHEN n.idadePaciente BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS age50to59,
                SUM(CASE WHEN n.idadePaciente BETWEEN 60 AND 69 THEN 1 ELSE 0 END) AS age60to69,
                SUM(CASE WHEN n.idadePaciente BETWEEN 70 AND 79 THEN 1 ELSE 0 END) AS age70to79,
                SUM(CASE WHEN n.idadePaciente BETWEEN 80 AND 89 THEN 1 ELSE 0 END) AS age80to89,
                SUM(CASE WHEN n.idadePaciente BETWEEN 90 AND 99 THEN 1 ELSE 0 END) AS age90to99
            FROM Notification n WHERE 1=1
        """);

        Map<String, Object> parameters = new LinkedHashMap<>();

        if (agravoId != null) {
            jpql.append(" AND n.idAgravo = :idAgravo");
            parameters.put("idAgravo", agravoId);
        }
        Optional<List<String>> classificacaoFilter = resolveClassificacaoFilter(agravoName);
        if (classificacaoFilter.isPresent()) {
            jpql.append(" AND n.classificacao IN :classificacoes");
            parameters.put("classificacoes", classificacaoFilter.get());
        }
        if (year != null) {
            jpql.append(" AND FUNCTION('date_part', 'year', n.dataPrimeiroSintoma) = :year");
            parameters.put("year", year.doubleValue());
        }
        if (bairro != null && !bairro.isEmpty()) {
            jpql.append(" AND n.nomeBairro = :nomeBairro");
            parameters.put("nomeBairro", bairro);
        }
        appendScopeClause(jpql, parameters, scope);

        var query = entityManager.createQuery(jpql.toString(), Tuple.class);
        parameters.forEach(query::setParameter);

        Tuple resultTuple;
        try {
            resultTuple = query.getSingleResult();
        } catch (NoResultException e) {
            resultTuple = null;
        }

        Map<String, Integer> resultMap = new LinkedHashMap<>();
        for (String bracket : ageBrackets) {
            if (resultTuple != null) {
                Long count = resultTuple.get(bracket, Long.class);
                resultMap.put(bracket, count != null ? count.intValue() : 0);
            } else {
                resultMap.put(bracket, 0);
            }
        }

        return resultMap;
    }

    public static List<BairroCountDTO> filtersForNotificationsCountNeighborhoods(
        HttpServletRequest request,
        NotificationRepository notificationRepository
    ) throws InvalidAgravoException {
        return notificationRepository.buscarContagemPorBairro(buildDashboardSpecification(request));
    }

    public static long filterForCountByIdAgravo(HttpServletRequest request, NotificationRepository notificationRepository) throws Exception {
        return notificationRepository.count(buildDashboardSpecification(request));
    }

    public static long filterForCountByEvolucao(HttpServletRequest request, NotificationRepository notificationRepository) throws Exception {
        String evolucao = Optional.ofNullable(request.getParameter("evolucao"))
                .orElseThrow(() -> new Exception("Informe o nivel da evolucao"));

        Specification<Notification> spec = buildDashboardSpecification(request)
            .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("evolucao"), evolucao));

        return notificationRepository.count(spec);
    }

    private static String resolveAgravoId(String agravoName) throws InvalidAgravoException {
        if (agravoName == null || agravoName.isEmpty()) {
            return null;
        }

        return ConvertNameToIdAgravo.convert(agravoName);
    }

    public static Optional<List<String>> resolveClassificacaoFilter(String agravoName) {
        if (agravoName == null || agravoName.isBlank()) return Optional.empty();
        return switch (agravoName.trim().toLowerCase()) {
            case "dengue_geral"     -> Optional.of(List.of("10", "11", "12", "13"));
            case "dengue_classica"  -> Optional.of(List.of("10"));
            case "dengue_alarmante" -> Optional.of(List.of("11"));
            case "dengue_grave"     -> Optional.of(List.of("12"));
            default                 -> Optional.empty();
        };
    }

    private static String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return "notificados";
        }

        return switch (scope.trim().toLowerCase()) {
            case "confirmados", "obitos", "notificados" -> scope.trim().toLowerCase();
            default -> "notificados";
        };
    }

    private static void applyScope(
        String scope,
        List<Predicate> predicates,
        jakarta.persistence.criteria.Root<Notification> root,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder
    ) {
        switch (scope) {
            case "confirmados" -> predicates.add(root.get("classificacao").in(CONFIRMED_CLASSIFICATIONS));
            case "obitos" -> predicates.add(criteriaBuilder.equal(root.get("evolucao"), "2"));
            default -> {
            }
        }
    }

    private static void appendScopeClause(StringBuilder jpql, Map<String, Object> parameters, String scope) {
        switch (scope) {
            case "confirmados" -> {
                jpql.append(" AND n.classificacao IN :confirmedClassifications");
                parameters.put("confirmedClassifications", CONFIRMED_CLASSIFICATIONS);
            }
            case "obitos" -> {
                jpql.append(" AND n.evolucao = :scopeEvolucao");
                parameters.put("scopeEvolucao", "2");
            }
            default -> {
            }
        }
    }
}
