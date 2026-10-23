package com.findsomeone.repository;

import com.findsomeone.entity.Meetup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetupRepository extends JpaRepository<Meetup, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Meetup m WHERE m.id = :meetupId")
    Optional<Meetup> findByIdForUpdate(
            @Param("meetupId") Long meetupId
    );
}
