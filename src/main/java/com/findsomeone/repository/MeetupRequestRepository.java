package com.findsomeone.repository;

import com.findsomeone.entity.MeetupRequest;
import com.findsomeone.entity.MeetupRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetupRequestRepository extends JpaRepository<MeetupRequest, Long> {

    boolean existsByMeetupIdAndRequesterIdAndStatusIn(
            Long meetupId,
            Long requesterId,
            List<MeetupRequestStatus> statuses
    );
    List<MeetupRequest> findAll();
}
