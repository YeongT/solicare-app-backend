package com.solicare.app.backend.domain.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    OTHER("성별미상");

    private final String text;
}
