package com.arboviroses.conectaDengue.Domain.Repositories.Notifications;

import org.springframework.data.jpa.domain.Specification;

import com.arboviroses.conectaDengue.Api.DTO.response.AgravoCountBySemanaEpidemiologica;
import com.arboviroses.conectaDengue.Api.DTO.response.BairroCountDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.util.List;

public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AgravoCountBySemanaEpidemiologica> buscarContagemPorSemanaEpidemiologica(Specification<Notification> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AgravoCountBySemanaEpidemiologica> query = cb.createQuery(AgravoCountBySemanaEpidemiologica.class);
        Root<Notification> root = query.from(Notification.class);

        if (spec != null) {
            query.where(spec.toPredicate(root, query, cb));
        }

        query.groupBy(root.get("semanaEpidemiologica"), root.get("idAgravo"));

        query.select(cb.construct(
            AgravoCountBySemanaEpidemiologica.class,
            root.get("semanaEpidemiologica"),
            cb.count(root),
            root.get("idAgravo")
        ));
        
        query.orderBy(cb.asc(root.get("semanaEpidemiologica")));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<BairroCountDTO> buscarContagemPorBairro(Specification<Notification> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BairroCountDTO> query = cb.createQuery(BairroCountDTO.class);
        Root<Notification> root = query.from(Notification.class);

        if (spec != null) {
            query.where(spec.toPredicate(root, query, cb));
        }

        query.groupBy(root.get("nomeBairro"));

        Expression<Long> total = cb.count(root);

        Expression<Long> evolucao1 = cb.sum(
            cb.<Long>selectCase()
                .when(cb.equal(root.get("evolucao"), "1"), 1L)
                .otherwise(0L)
        );

        Expression<Long> evolucao2 = cb.sum(
            cb.<Long>selectCase()
                .when(cb.equal(root.get("evolucao"), "2"), 1L)
                .otherwise(0L)
        );

        Expression<Long> evolucao9 = cb.sum(
            cb.<Long>selectCase()
                .when(cb.equal(root.get("evolucao"), "9"), 1L)
                .otherwise(0L)
        );

        query.select(cb.construct(
            BairroCountDTO.class,
            root.get("nomeBairro"),
            total,
            evolucao1,
            evolucao2,
            evolucao9
        ));

        query.orderBy(cb.desc(total));

        return entityManager.createQuery(query).getResultList();
    }

}
