package com.findsomeone.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "meetup_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"meetup_id", "user_id"}
                )
        }
)
public class MeetupParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Meetup meetup;

    @ManyToOne(optional = false)
    private User user;

    public Long getId() {
        return id;
    }

    public Meetup getMeetup() {
        return meetup;
    }

    public void setMeetup(Meetup meetup) {
        this.meetup = meetup;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
