package com.solicare.app.backend.application.enums;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "시니어 모니터링 이벤트 유형")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SeniorEvent {
    FALL_DETECTED("낙상이 감지되었습니다."),
    CAMERA_BATTERY_LOW("카메라 배터리가 부족합니다."),
    CAMERA_DISCONNECTED("카메라 연결이 끊어졌습니다."),
    WEARABLE_BATTERY_LOW("웨어러블 기기 배터리가 부족합니다."),
    WEARABLE_DISCONNECTED("웨어러블 기기 연결이 끊어졌습니다."),
    INACTIVITY_ALERT("장시간 움직임이 감지되지 않습니다.");

    private final String message;
}
