package com.findsomeone.controller;

import com.findsomeone.dto.MeetupRequestResponse;
import com.findsomeone.service.MeetupRequestService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MeetupRequestController {

    private final MeetupRequestService meetupRequestService;

    public MeetupRequestController(
            MeetupRequestService meetupRequestService) {
        this.meetupRequestService = meetupRequestService;
    }

    @GetMapping("/meetup-requests")
    public List<MeetupRequestResponse> myRequests() {
        return meetupRequestService.myRequests(currentUserId());
    }

    @GetMapping("/meetups/{meetupId}/requests")
    public List<MeetupRequestResponse> requestsForMeetup(
            @PathVariable Long meetupId) {
        return meetupRequestService.requestsForMeetup(meetupId, currentUserId());
    }

    @PostMapping("/meetups/{meetupId}/requests")
    public MeetupRequestResponse requestToJoin(
            @PathVariable Long meetupId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        return meetupRequestService.requestToJoin(meetupId, userId);
    }

    @PatchMapping("/meetup-requests/{requestId}/accept")
    public MeetupRequestResponse acceptRequest(
            @PathVariable Long requestId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        return meetupRequestService.acceptRequest(requestId, userId);
    }

    @DeleteMapping("/meetup-requests/{requestId}")
    public void deleteRequest(
            @PathVariable Long requestId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        meetupRequestService.deleteRequest(requestId, userId);
    }

    @PatchMapping("/meetup-requests/{requestId}/reject")
    public MeetupRequestResponse rejectRequest(
            @PathVariable Long requestId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        return meetupRequestService.rejectRequest(requestId, userId);
    }

    private Long currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }


}
