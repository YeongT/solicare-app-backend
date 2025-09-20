package com.solicare.app.backend.application.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MedicineResponseDTO {
    @Schema(name = "MedicineInfoResponse", description = "약 단일 조회 응답 DTO")
    public record Info(
            @Schema(description = "약 이름") String name,
            @Schema(description = "약 설명") String description,
            @Schema(description = "복용량") Double doseAmount,
            @Schema(description = "복용 지침") String doseInstruction,
            @Schema(description = "복용 요일") Set<DayOfWeek> daysOfWeek,
            @Schema(description = "복용 시간") LocalTime intakeTime,
            @Schema(description = "메모") String memo,
            @Schema(description = "시간 카테고리") Set<String> timeCategories) {}

    @Schema(name = "MedicineIntakeHistoryResponse", description = "약 복용 이력 응답 DTO")
    public record IntakeHistory(
            @Schema(description = "이력 UUID") String uuid,
            @Schema(description = "기록 시각") LocalDateTime recordedAt,
            @Schema(description = "복용량") Double intakeAmount,
            @Schema(description = "복용 상태") String status // TAKEN, SKIPPED, MISSED
            ) {}

    @Schema(name = "MedicineDetailedInfoResponse", description = "약 상세 조회 응답")
    public record DetailedInfo(Info medicine, List<IntakeHistory> history) {}
}
