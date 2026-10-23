package com.findsomeone.service;

import com.findsomeone.dto.NearbyMeetupResponse;
import com.findsomeone.entity.Meetup;
import com.findsomeone.entity.MeetupParticipant;
import com.findsomeone.entity.MeetupStatus;
import com.findsomeone.entity.User;
import com.findsomeone.repository.MeetupParticipantRepository;
import com.findsomeone.repository.MeetupRepository;
import com.findsomeone.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MeetupService {

    private final MeetupRepository meetupRepository;
    private final UserRepository userRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;

    public MeetupService(
            MeetupRepository meetupRepository,
            UserRepository userRepository,
            MeetupParticipantRepository meetupParticipantRepository) {

        this.meetupRepository = meetupRepository;
        this.userRepository = userRepository;
        this.meetupParticipantRepository = meetupParticipantRepository;
    }


    public Meetup createMeetup(Meetup meetup, Long creatorId) {

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        meetup.setCreator(creator);
        meetup.setStatus(MeetupStatus.OPEN);

        Meetup savedMeetup = meetupRepository.save(meetup);

        // Add creator as the first participant
        MeetupParticipant participant = new MeetupParticipant();
        participant.setMeetup(savedMeetup);
        participant.setUser(creator);

        meetupParticipantRepository.save(participant);

        return savedMeetup;
    }

    public Meetup getMeetup(Long id) {
        return meetupRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meetup not found"
                        ));
    }

    public List<Meetup> getAllMeetups() {
        return meetupRepository.findAll();
    }


    public void updateMeetupStatus(Long meetupId, Long userId) {

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meetup not found"
                        ));

        if (!meetup.getCreator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the meetup creator can update its status"
            );
        }

        long participantCount =
                meetupParticipantRepository.countByMeetupId(meetupId);

        if (participantCount >= meetup.getMaxParticipants()) {
            meetup.setStatus(MeetupStatus.FULL);
        } else {
            meetup.setStatus(MeetupStatus.OPEN);
        }

        meetupRepository.save(meetup);
    }



    public List<Meetup> getNearbyMeetups(
            double latitude,
            double longitude,
            double radiusKm) {

        return meetupRepository.findAll()
                .stream()
                .filter(meetup -> meetup.getLatitude() != null
                        && meetup.getLongitude() != null)
                .filter(meetup -> meetup.getStatus() == MeetupStatus.OPEN)
                .filter(meetup ->
                        meetupParticipantRepository.countByMeetupId(meetup.getId())
                                < meetup.getMaxParticipants())
                .filter(meetup -> !meetup.getDate().isBefore(
                        java.time.LocalDate.now()))
                .filter(meetup -> {
                    double distance = calculateDistance(
                            latitude,
                            longitude,
                            meetup.getLatitude(),
                            meetup.getLongitude()
                    );

                    return distance <= radiusKm;
                }).sorted((m1, m2) -> Double.compare(
                        calculateDistance(
                                latitude,
                                longitude,
                                m1.getLatitude(),
                                m1.getLongitude()
                        ),
                        calculateDistance(
                                latitude,
                                longitude,
                                m2.getLatitude(),
                                m2.getLongitude()
                        )
                ))

                .toList();
    }

    private double calculateDistance(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {

        double earthRadiusKm = 6371.0;

        double latDistance = Math.toRadians(latitude2 - latitude1);
        double lonDistance = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }


    public List<NearbyMeetupResponse> getNearbyMeetupResponses(
            double latitude,
            double longitude,
            double radiusKm) {

        return getNearbyMeetups(latitude, longitude, radiusKm)
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
                        Math.round(
                                calculateDistance(
                                        latitude,
                                        longitude,
                                        meetup.getLatitude(),
                                        meetup.getLongitude()
                                ) * 100.0
                        ) / 100.0,
                        (int) (meetup.getMaxParticipants()
                                - meetupParticipantRepository
                                .countByMeetupId(meetup.getId()))
                ))
                .toList();
    }


    public double getDistanceKm(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {

        return calculateDistance(
                latitude1,
                longitude1,
                latitude2,
                longitude2
        );
    }

    public int getAvailableSpots(Meetup meetup) {
        long totalParticipants =
                meetupParticipantRepository.countByMeetupId(meetup.getId());

        return (int) (meetup.getMaxParticipants() - totalParticipants);
    }

    public void cancelMeetup(Long meetupId, Long userId) {

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meetup not found"
                        ));

        if (!meetup.getCreator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the meetup creator can cancel this meetup"
            );
        }

        if (meetup.getStatus() == MeetupStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Meetup is already cancelled"
            );
        }

        if (meetup.getStatus() == MeetupStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed meetup cannot be cancelled"
            );
        }

        meetup.setStatus(MeetupStatus.CANCELLED);
        meetupRepository.save(meetup);
    }

}
