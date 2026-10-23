package com.findsomeone.repository;

import com.findsomeone.entity.MeetupRequest;
import com.findsomeone.entity.MeetupRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetupRequestRepository extends JpaRepository<MeetupRequest, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MeetupRequest r where r.id = :requestId")
    Optional<MeetupRequest> findByIdForUpdate(@Param("requestId") Long requestId);

    boolean existsByMeetupIdAndRequesterIdAndStatusIn(
            Long meetupId,
            Long requesterId,
            List<MeetupRequestStatus> statuses
    );
    List<MeetupRequest> findByRequesterIdOrderByIdDesc(Long requesterId);

    List<MeetupRequest> findByMeetupIdOrderByIdDesc(Long meetupId);
}
