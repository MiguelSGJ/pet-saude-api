package com.arboviroses.conectaDengue.Domain.Repositories.Notifications;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification>, NotificationRepositoryCustom
{
    Page<Notification> findByIdAgravo(Pageable pageable, String idAgravo);

    @Query("""
        SELECT n
        FROM Notification n
        WHERE FUNCTION('date_part', 'year', n.dataNotification) = :year
        AND n.idAgravo = :idAgravo
        """)
    List<Notification> findByYearAndIdAgravo(int year, String idAgravo);

    @Query("SELECT COALESCE(MAX(n.idNotification), 0) FROM Notification n")
    Optional<Long> findMaxId();

    @Query("SELECT MAX(n.dataNotification) FROM Notification n")
    Optional<Date> findMaxDate();
}