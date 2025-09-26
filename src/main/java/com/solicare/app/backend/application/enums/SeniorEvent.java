package com.solicare.app.backend.application.enums;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "시니어 모니터링 이벤트 유형")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SeniorEvent {
    FALL_DETECTED("낙상 감지", "낙상이 감지되었습니다.\n대시보드를 확인하여 시니어의 상태를 확인하세요."),
    CAMERA_BATTERY_LOW("디바이스 경고", "카메라 배터리가 부족합니다.\n카메라 기기에 충전기를 연결해주세요."),
    CAMERA_DISCONNECTED("장치분리 감지", "카메라 연결이 끊어졌습니다.\n카메라 기기 동작상태를 확인해주세요."),
    WEARABLE_BATTERY_LOW("디바이스 경고", "웨어러블 기기 배터리가 부족합니다.\n웨어러블 기기에 충전기를 연결해주세요."),
    WEARABLE_DISCONNECTED("장치분리 감지", "웨어러블 기기 연결이 끊어졌습니다.\n웨어러블 기기 동작상태를 확인해주세요."),
    INACTIVITY_ALERT("비활동 감지", "장시간동안 시니어의 움직임이 감지되지 않습니다.\n시니어의 상태를 직접 확인해주세요.");

    private final String title;
    private final String message;
}
