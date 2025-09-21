package com.solicare.app.backend.application.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum IntakeTime {
    MORNING("아침", LocalTime.of(6, 0), LocalTime.of(9, 0)),
    AFTERNOON("점심", LocalTime.of(11, 0), LocalTime.of(14, 0)),
    EVENING("저녁", LocalTime.of(17, 0), LocalTime.of(20, 0)),
    NIGHT("취침 전", LocalTime.of(21, 0), LocalTime.of(23, 0));

    private final String value;
    private final LocalTime timeRangeStart;
    private final LocalTime timeRangeEnd;
}
