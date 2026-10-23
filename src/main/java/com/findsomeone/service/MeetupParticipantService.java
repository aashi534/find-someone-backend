package com.findsomeone.service;

import com.findsomeone.entity.MeetupParticipant;
import com.findsomeone.repository.MeetupParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetupParticipantService {

    private final MeetupParticipantRepository meetupParticipantRepository;

    public MeetupParticipantService(
            MeetupParticipantRepository meetupParticipantRepository) {
        this.meetupParticipantRepository = meetupParticipantRepository;
    }

    public MeetupParticipant addParticipant(MeetupParticipant participant) {
        return meetupParticipantRepository.save(participant);
    }

    public List<MeetupParticipant> getParticipants(Long meetupId) {
        return meetupParticipantRepository.findAll()
                .stream()
                .filter(p -> p.getMeetup().getId().equals(meetupId))
                .toList();
    }
}
