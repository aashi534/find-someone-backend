package com.findsomeone.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import com.findsomeone.entity.MeetupCategory;
import com.findsomeone.entity.MeetupStyle;

public class NearbyMeetupResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationName;
    private MeetupCategory category;
    private MeetupStyle style;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Integer availableSpots;

    public NearbyMeetupResponse(
            Long id,
            String title,
            String description,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String locationName,
            MeetupCategory category,
            MeetupStyle style,
            Double latitude,
            Double longitude,
            Double distanceKm,
            Integer availableSpots) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.locationName = locationName;
        this.category = category;
        this.style = style;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.availableSpots = availableSpots;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLocationName() {
        return locationName;
    }

    public MeetupCategory getCategory() {
        return category;
    }

    public MeetupStyle getStyle() {
        return style;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public Integer getAvailableSpots() {
        return availableSpots;
    }
}
