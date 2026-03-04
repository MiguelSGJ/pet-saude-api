package com.arboviroses.conectaDengue.Domain.Repositories.Notifications;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.NotificationWithError;

@Repository
public interface NotificationWithErrorRepository extends JpaRepository<NotificationWithError, Long> {
    Optional<NotificationWithError> findTopByOrderByIterationDesc();

    @Query("SELECT n FROM NotificationWithError n WHERE n.iteration = (SELECT MAX(n2.iteration) FROM NotificationWithError n2)")
    List<NotificationWithError> findAllWithMaxIteration();

    @Query("SELECT n FROM NotificationWithError n WHERE n.iteration = (SELECT MAX(n2.iteration) FROM NotificationWithError n2)")
    Page<NotificationWithError> findAllWithMaxIteration(Pageable pageable);
}
