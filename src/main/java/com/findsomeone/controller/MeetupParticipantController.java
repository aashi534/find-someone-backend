package com.findsomeone.controller;

import com.findsomeone.entity.MeetupParticipant;
import com.findsomeone.service.MeetupParticipantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MeetupParticipantController {

    private final MeetupParticipantService meetupParticipantService;

    public MeetupParticipantController(
            MeetupParticipantService meetupParticipantService) {
        this.meetupParticipantService = meetupParticipantService;
    }

    @GetMapping("/meetups/{meetupId}/participants")
    public List<MeetupParticipant> getParticipants(
            @PathVariable Long meetupId) {

        return meetupParticipantService.getParticipants(meetupId);
    }
}
