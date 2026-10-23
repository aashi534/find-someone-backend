package com.findsomeone.entity;

import jakarta.persistence.*;

@Entity
public class MeetupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User requester;

    @ManyToOne(optional = false)
    private Meetup meetup;

    @Enumerated(EnumType.STRING)
    private MeetupRequestStatus status = MeetupRequestStatus.PENDING;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public Meetup getMeetup() {
        return meetup;
    }

    public void setMeetup(Meetup meetup) {
        this.meetup = meetup;
    }

    public MeetupRequestStatus getStatus() {
        return status;
    }

    public void setStatus(MeetupRequestStatus status) {
        this.status = status;
    }
}
