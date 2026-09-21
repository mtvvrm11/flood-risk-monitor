package org.example.repository;

import org.example.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/*
 * jpa repository for notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByStoreIdAndSentAtAfter(Long storeId, LocalDateTime since);
}