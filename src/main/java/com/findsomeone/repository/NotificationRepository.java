package com.findsomeone.repository;

import com.findsomeone.entity.AppNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {

    List<AppNotification> findByRecipientIdOrderByCreatedAtDesc(
            Long recipientId, Pageable pageable);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);

    Optional<AppNotification> findByIdAndRecipientId(Long id, Long recipientId);

    @Modifying
    @Query("update AppNotification n set n.readAt = :readAt "
            + "where n.recipient.id = :recipientId and n.readAt is null")
    int markAllRead(@Param("recipientId") Long recipientId,
                    @Param("readAt") Instant readAt);
}
