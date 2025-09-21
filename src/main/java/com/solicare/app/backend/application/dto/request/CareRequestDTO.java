package com.solicare.app.backend.application.dto.request;

import com.solicare.app.backend.application.enums.MonitorMode;
import com.solicare.app.backend.application.enums.SeniorEvent;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CareRequestDTO {
    @Schema(name = "SeniorSensorStatPostRequest", description = "시니어 센서 통계 등록 요청 DTO")
    public record PostSensorStat(
            @Schema(
                            description = "측정 시각",
                            example = "2025-09-20T12:34:56",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "측정 시각은 필수입니다.")
                    LocalDateTime timestamp,
            @Schema(
                            description = "카메라 낙상 감지 여부",
                            example = "true",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "카메라 낙상 감지 여부는 필수입니다.")
                    Boolean cameraFallDetected,
            @Schema(
                            description = "웨어러블 낙상 감지 여부",
                            example = "false",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "웨어러블 낙상 감지 여부는 필수입니다.")
                    Boolean wearableFallDetected,
            @Schema(
                            description = "온도",
                            example = "36.5",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "온도는 필수입니다.")
                    Double temperature,
            @Schema(
                            description = "습도",
                            example = "45.0",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "습도는 필수입니다.")
                    Double humidity,
            @Schema(
                            description = "심박수",
                            example = "80",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "심박수는 필수입니다.")
                    Integer heartRate,
            @Schema(
                            description = "웨어러블 배터리",
                            example = "85.5",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "웨어러블 배터리는 필수입니다.")
                    Double wearableBattery) {}

    @Schema(name = "CareAlertPostRequest", description = "시니어 이벤트(알림) 등록 요청 DTO")
    public record PostCareAlert(
            @Schema(
                            description = "이벤트 발생 시각",
                            example = "2025-09-20T12:34:56",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "이벤트 발생 시각은 필수입니다.")
                    LocalDateTime timestamp,
            @Schema(
                            description = "이벤트 타입",
                            example = "FALL_DETECTED",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "이벤트 타입은 필수입니다.")
                    SeniorEvent eventType,
            @Schema(
                            description = "모니터링 모드",
                            example = "FULL_MONITORING",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "모니터링 모드는 필수입니다.")
                    MonitorMode monitorMode,
            @Schema(
                            description = "이벤트 이미지(Base64)",
                            example = "data:image/png;base64,...",
                            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    String base64Image,
            @Schema(
                            description = "읽음 여부",
                            example = "false",
                            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    Boolean isRead,
            @Schema(
                            description = "해제 여부",
                            example = "false",
                            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    Boolean isDismissed) {}
}
