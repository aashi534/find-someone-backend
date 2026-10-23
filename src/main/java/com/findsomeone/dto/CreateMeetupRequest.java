package com.findsomeone.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.findsomeone.entity.MeetupCategory;
import com.findsomeone.entity.MeetupStyle;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateMeetupRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    private LocalTime endTime;

    @NotBlank
    private String locationName;

    private Double latitude;

    private Double longitude;

    @NotNull
    @Min(2)
    private Integer maxParticipants;

    private MeetupCategory category = MeetupCategory.OTHER;

    private MeetupStyle style = MeetupStyle.INFORMAL;

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

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public MeetupCategory getCategory() {
        return category;
    }

    public void setCategory(MeetupCategory category) {
        this.category = category;
    }

    public MeetupStyle getStyle() {
        return style;
    }

    public void setStyle(MeetupStyle style) {
        this.style = style;
    }
}
