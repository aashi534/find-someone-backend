package com.findsomeone.repository;

import com.findsomeone.entity.MeetupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetupParticipantRepository
        extends JpaRepository<MeetupParticipant, Long> {

    boolean existsByMeetupIdAndUserId(Long meetupId, Long userId);

    long countByMeetupId(Long meetupId);
}