package com.findsomeone.repository;

import com.findsomeone.entity.ConnectionRequest;
import com.findsomeone.entity.ConnectionRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {
    boolean existsByRequesterIdAndRecipientIdAndStatus(
            Long requesterId, Long recipientId, ConnectionRequestStatus status);

    boolean existsByRequesterIdAndRecipientIdAndStatusIn(
            Long requesterId, Long recipientId, List<ConnectionRequestStatus> statuses);

    List<ConnectionRequest> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<ConnectionRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @Query("select r from ConnectionRequest r where "
            + "((r.requester.id = :userOneId and r.recipient.id = :userTwoId) or "
            + "(r.requester.id = :userTwoId and r.recipient.id = :userOneId)) "
            + "and r.status = :status")
    List<ConnectionRequest> findBetweenUsersWithStatus(
            @Param("userOneId") Long userOneId,
            @Param("userTwoId") Long userTwoId,
            @Param("status") ConnectionRequestStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ConnectionRequest r where r.id = :id")
    Optional<ConnectionRequest> findByIdForUpdate(@Param("id") Long id);
}
