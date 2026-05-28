package com.arboviroses.conectaDengue.Domain.Repositories.Notifications;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification>, NotificationRepositoryCustom
{
    Page<Notification> findByIdAgravo(Pageable pageable, String idAgravo);

    @Query("""
        SELECT n
        FROM Notification n
        WHERE FUNCTION('date_part', 'year', n.dataPrimeiroSintoma) = :year
        AND n.idAgravo = :idAgravo
        """)
    List<Notification> findByYearAndIdAgravo(int year, String idAgravo);

    @Query("SELECT COALESCE(MAX(n.idNotification), 0) FROM Notification n")
    Optional<Long> findMaxId();

    @Query("SELECT MAX(n.dataPrimeiroSintoma) FROM Notification n")
    Optional<Date> findMaxDate();

    @Query("SELECT MAX(n.dataPrimeiroSintoma) FROM Notification n WHERE n.idAgravo = :idAgravo")
    Optional<Date> findMaxDateByAgravo(@Param("idAgravo") String idAgravo);

    @Query("""
        SELECT n FROM Notification n
        WHERE (:year IS NULL OR FUNCTION('date_part', 'year', n.dataNotification) = :year)
        AND (:week IS NULL OR n.semanaEpidemiologica = :week)
        AND (:bairroPattern = '%' OR LOWER(n.nomeBairro) LIKE LOWER(:bairroPattern))
        AND (:idAgravo IS NULL OR n.idAgravo = :idAgravo)
        ORDER BY n.dataNotification DESC
        """)
    Page<Notification> findWithFilters(
        @Param("year") Integer year,
        @Param("week") Integer week,
        @Param("bairroPattern") String bairroPattern,
        @Param("idAgravo") String idAgravo,
        Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.dataNotification IN :dates")
    void deleteByDataNotificationIn(@Param("dates") Collection<Date> dates);
}