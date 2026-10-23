package com.findsomeone.controller;

import com.findsomeone.dto.CreateMeetupRequest;
import com.findsomeone.dto.NearbyMeetupResponse;
import com.findsomeone.entity.Meetup;
import com.findsomeone.service.MeetupService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MeetupController {

    private final MeetupService meetupService;

    public MeetupController(MeetupService meetupService) {
        this.meetupService = meetupService;
    }

    @PostMapping("/meetups")
    public Meetup createMeetup(
            @Valid @RequestBody CreateMeetupRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long creatorId = (Long) authentication.getPrincipal();

        Meetup meetup = new Meetup();

        meetup.setTitle(request.getTitle());
        meetup.setDescription(request.getDescription());
        meetup.setDate(request.getDate());
        meetup.setStartTime(request.getStartTime());
        meetup.setEndTime(request.getEndTime());
        meetup.setLocationName(request.getLocationName());
        meetup.setLatitude(request.getLatitude());
        meetup.setLongitude(request.getLongitude());
        meetup.setMaxParticipants(request.getMaxParticipants());
        meetup.setCategory(request.getCategory());
        meetup.setStyle(request.getStyle());

        return meetupService.createMeetup(meetup, creatorId);
    }

    @GetMapping("/meetups/{id}")
    public Meetup getMeetup(@PathVariable Long id) {
        return meetupService.getMeetup(id);
    }

    @GetMapping("/meetups")
    public List<Meetup> getAllMeetups() {
        return meetupService.getAllMeetups();
    }


    @PatchMapping("/meetups/{meetupId}/status")
    public void updateMeetupStatus(@PathVariable Long meetupId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        meetupService.updateMeetupStatus(meetupId, userId);
    }


    @GetMapping("/meetups/nearby")
    public List<NearbyMeetupResponse> getNearbyMeetups(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate date,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    pattern = "HH:mm:ss")
            java.time.LocalTime startTime,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    pattern = "HH:mm:ss")
            java.time.LocalTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid latitude or longitude"
            );
        }

        if (radiusKm <= 0 || radiusKm > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "radiusKm must be greater than 0 and at most 100"
            );
        }

        if (startTime != null && endTime != null
                && startTime.isAfter(endTime)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "startTime must be before or equal to endTime"
            );
        }

        if (page < 0 || size < 1 || size > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "page must be >= 0 and size must be between 1 and 100"
            );
        }

        List<Meetup> meetups = meetupService.getNearbyMeetups(
                latitude, longitude, radiusKm
        );

        List<Meetup> filteredMeetups = meetups.stream()
                .filter(meetup ->
                        date == null || meetup.getDate().equals(date))
                .filter(meetup ->
                        startTime == null
                                || !meetup.getStartTime().isBefore(startTime))
                .filter(meetup ->
                        endTime == null
                                || !meetup.getStartTime().isAfter(endTime))
                .filter(meetup ->
                        meetupService.getAvailableSpots(meetup) > 0)
                .toList();

        int fromIndex = Math.min(page * size, filteredMeetups.size());
        int toIndex = Math.min(fromIndex + size, filteredMeetups.size());

        return filteredMeetups.subList(fromIndex, toIndex)
                .stream()
                .map(meetup -> new NearbyMeetupResponse(
                        meetup.getId(),
                        meetup.getTitle(),
                        meetup.getDescription(),
                        meetup.getDate(),
                        meetup.getStartTime(),
                        meetup.getEndTime(),
                        meetup.getLocationName(),
                        meetup.getCategory(),
                        meetup.getStyle(),
                        meetup.getLatitude(),
                        meetup.getLongitude(),
                        meetupService.getDistanceKm(
                                latitude,
                                longitude,
                                meetup.getLatitude(),
                                meetup.getLongitude()
                        ),
                        meetupService.getAvailableSpots(meetup)
                ))
                .toList();
    }


    @PatchMapping("/meetups/{meetupId}/cancel")
    public void cancelMeetup(@PathVariable Long meetupId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        meetupService.cancelMeetup(meetupId, userId);
    }



}
