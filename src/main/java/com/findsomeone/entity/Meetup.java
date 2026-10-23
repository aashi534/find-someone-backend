package com.findsomeone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "meetups")
public class Meetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User creator;

    private String title;
    private String description;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private String locationName;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'OTHER'")
    private MeetupCategory category = MeetupCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'INFORMAL'")
    private MeetupStyle style = MeetupStyle.INFORMAL;

    @Enumerated(EnumType.STRING)
    private MeetupStatus status;

    @NotNull
    private Integer maxParticipants;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public MeetupCategory getCategory() {
        return category;
    }

    public void setCategory(MeetupCategory category) {
        this.category = category == null ? MeetupCategory.OTHER : category;
    }

    public MeetupStyle getStyle() {
        return style;
    }

    public void setStyle(MeetupStyle style) {
        this.style = style == null ? MeetupStyle.INFORMAL : style;
    }

    public MeetupStatus getStatus() {
        return status;
    }

    public void setStatus(MeetupStatus status) {
        this.status = status;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
}
