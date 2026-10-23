package com.findsomeone.service;

import com.findsomeone.entity.*;
import com.findsomeone.dto.MeetupRequestResponse;
import com.findsomeone.repository.MeetupParticipantRepository;
import com.findsomeone.repository.MeetupRepository;
import com.findsomeone.repository.MeetupRequestRepository;
import com.findsomeone.repository.UserRepository;
import com.findsomeone.repository.UserBlockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MeetupRequestService {

    private final MeetupRequestRepository meetupRequestRepository;
    private final UserRepository userRepository;
    private final MeetupRepository meetupRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;
    private final UserBlockRepository userBlockRepository;

    public MeetupRequestService(
            MeetupRequestRepository meetupRequestRepository,
            UserRepository userRepository,
            MeetupRepository meetupRepository,
            MeetupParticipantRepository meetupParticipantRepository,
            UserBlockRepository userBlockRepository) {

        this.meetupRequestRepository = meetupRequestRepository;
        this.userRepository = userRepository;
        this.meetupRepository = meetupRepository;
        this.meetupParticipantRepository = meetupParticipantRepository;
        this.userBlockRepository = userBlockRepository;
    }

    @Transactional
    public MeetupRequestResponse requestToJoin(Long meetupId, Long userId) {

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meetup not found"
                        ));

        if (meetup.getStatus() == MeetupStatus.CANCELLED
                || meetup.getStatus() == MeetupStatus.COMPLETED
                || meetup.getStatus() == MeetupStatus.EXPIRED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot request to join an inactive meetup"
            );
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        if (meetup.getCreator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Creator cannot request to join their own meetup"
            );
        }

        if (userBlockRepository.existsBetweenUsers(
                userId, meetup.getCreator().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Meetup not found");
        }

        if (meetupRequestRepository.existsByMeetupIdAndRequesterIdAndStatusIn(
                meetupId,
                userId,
                List.of(
                        MeetupRequestStatus.PENDING,
                        MeetupRequestStatus.ACCEPTED
                )
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Join request already exists"
            );
        }

        long participantCount =
                meetupParticipantRepository.countByMeetupId(meetupId);

        if (meetup.getStatus() == MeetupStatus.FULL
                || participantCount >= meetup.getMaxParticipants()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Meetup is full"
            );
        }


        MeetupRequest request = new MeetupRequest();

        request.setMeetup(meetup);
        request.setRequester(user);
        request.setStatus(MeetupRequestStatus.PENDING);

        return toResponse(meetupRequestRepository.save(request));
    }

    @Transactional
    public MeetupRequestResponse acceptRequest(Long requestId, Long userId) {

        MeetupRequest request = meetupRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meetup request not found"
                        ));

        Meetup meetup = meetupRepository.findByIdForUpdate(
                request.getMeetup().getId()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meetup not found"
                ));

        if (!request.getMeetup().getCreator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only meetup creator can accept requests"
            );
        }

        if (request.getStatus() != MeetupRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending requests can be accepted"
            );
        }

        if (userBlockRepository.existsBetweenUsers(
                userId, request.getRequester().getId())) {
            request.setStatus(MeetupRequestStatus.REJECTED);
            return toResponse(meetupRequestRepository.save(request));
        }


        if (meetup.getStatus() == MeetupStatus.CANCELLED
                || meetup.getStatus() == MeetupStatus.COMPLETED
                || meetup.getStatus() == MeetupStatus.EXPIRED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot accept requests for an inactive meetup"
            );
        }


        long participantCount =
                meetupParticipantRepository.countByMeetupId(meetup.getId());

        if (participantCount >= meetup.getMaxParticipants()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Meetup is full"
            );
        }

        if (meetupParticipantRepository.existsByMeetupIdAndUserId(
                request.getMeetup().getId(),
                request.getRequester().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already a participant"
            );
        }

        request.setStatus(MeetupRequestStatus.ACCEPTED);

        MeetupParticipant participant = new MeetupParticipant();
        participant.setMeetup(request.getMeetup());
        participant.setUser(request.getRequester());

        meetupParticipantRepository.save(participant);

        long updatedParticipantCount =
                meetupParticipantRepository.countByMeetupId(
                        request.getMeetup().getId()
                );

        if (updatedParticipantCount >= meetup.getMaxParticipants()) {
            meetup.setStatus(MeetupStatus.FULL);
            meetupRepository.save(meetup);
        }

        return toResponse(meetupRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<MeetupRequestResponse> myRequests(Long userId) {
        return meetupRequestRepository.findByRequesterIdOrderByIdDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MeetupRequestResponse> requestsForMeetup(Long meetupId, Long userId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meetup not found"));
        if (!meetup.getCreator().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the meetup creator can view join requests");
        }
        return meetupRequestRepository.findByMeetupIdOrderByIdDesc(meetupId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteRequest(Long requestId, Long userId) {

        MeetupRequest request = meetupRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meetup request not found"
                        ));

        Long requesterId = request.getRequester().getId();
        if (!requesterId.equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the requester can cancel their join request"
            );
        }

        if (request.getStatus() != MeetupRequestStatus.PENDING
                && request.getStatus() != MeetupRequestStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only pending or accepted requests can be cancelled");
        }

        if (request.getStatus() == MeetupRequestStatus.ACCEPTED) {
            Meetup meetup = meetupRepository.findByIdForUpdate(
                    request.getMeetup().getId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Meetup not found"));
            meetupParticipantRepository.deleteByMeetupIdAndUserId(
                    meetup.getId(), requesterId);
            if (meetup.getStatus() == MeetupStatus.FULL
                    && meetupParticipantRepository.countByMeetupId(meetup.getId())
                    < meetup.getMaxParticipants()) {
                meetup.setStatus(MeetupStatus.OPEN);
                meetupRepository.save(meetup);
            }
        }

        request.setStatus(MeetupRequestStatus.CANCELLED);
        meetupRequestRepository.save(request);
    }

    @Transactional
    public MeetupRequestResponse rejectRequest(Long requestId, Long userId) {

        MeetupRequest request = meetupRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meetup request not found"
                        ));

        if (!request.getMeetup().getCreator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only meetup creator can reject requests"
            );
        }

        if (request.getStatus() != MeetupRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending requests can be rejected"
            );
        }

        request.setStatus(MeetupRequestStatus.REJECTED);

        return toResponse(meetupRequestRepository.save(request));
    }

    private MeetupRequestResponse toResponse(MeetupRequest request) {
        return new MeetupRequestResponse(request.getId(),
                request.getMeetup().getId(), request.getMeetup().getTitle(),
                request.getRequester().getId(), request.getRequester().getName(),
                request.getStatus());
    }
}
