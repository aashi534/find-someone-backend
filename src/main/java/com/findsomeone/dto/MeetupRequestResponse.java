package com.findsomeone.dto;

import com.findsomeone.entity.MeetupRequestStatus;

public record MeetupRequestResponse(
        Long id,
        Long meetupId,
        String meetupTitle,
        Long requesterId,
        String requesterName,
        MeetupRequestStatus status) {
}
