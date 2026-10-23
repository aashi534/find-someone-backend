package com.findsomeone.controller;

import com.findsomeone.entity.MeetupRequest;
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
    public List<MeetupRequest> getAllRequests() {
        return meetupRequestService.getAllRequests();
    }

    @PostMapping("/meetups/{meetupId}/requests")
    public MeetupRequest requestToJoin(
            @PathVariable Long meetupId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        return meetupRequestService.requestToJoin(meetupId, userId);
    }

    @PatchMapping("/meetup-requests/{requestId}/accept")
    public MeetupRequest acceptRequest(
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
    public MeetupRequest rejectRequest(
            @PathVariable Long requestId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        return meetupRequestService.rejectRequest(requestId, userId);
    }

    @PatchMapping("/{requestId}/reject")
    public MeetupRequest rejectRequest(
            @PathVariable Long requestId,
            @RequestParam Long userId) {
        return meetupRequestService.rejectRequest(requestId, userId);
    }



}
