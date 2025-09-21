package com.solicare.app.backend.application.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SeniorMonitorMode", description = "시니어 모니터링 모드")
public enum MonitorMode {
    FULL_MONITORING,
    CAMERA_ONLY,
    WEARABLE_ONLY,
    NO_MONITORING
}
