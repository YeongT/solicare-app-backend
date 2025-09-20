package com.solicare.app.backend.application.dto.request;

import com.solicare.app.backend.domain.entity.MedicineHistory;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicineRequestDTO {
    @Schema(name = "MedicineCreateRequest", description = "약 등록 요청 DTO")
    public record Create(
            @Schema(description = "약 이름", example = "타이레놀") @NotBlank(message = "약 이름은 필수입니다.")
                    String name,
            @Schema(description = "약 설명", example = "해열진통제") @NotBlank(message = "약 설명은 필수입니다.")
                    String description,
            @Schema(description = "복용량", example = "1.0") @NotNull(message = "복용량은 필수입니다.")
                    Double doseAmount,
            @Schema(description = "복약지도", example = "식후 30분에 복용하세요")
                    @NotBlank(message = "복약지도는 필수입니다.")
                    String doseInstruction,
            @Schema(description = "복용 요일", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
                    @NotEmpty(message = "복용 요일은 하나 이상 선택해야 합니다.")
                    Set<DayOfWeek> daysOfWeek,
            @Schema(description = "복용 시간", example = "[\"08:00\", \"20:00\"]")
                    @NotEmpty(message = "복용 시간은 하나 이상 선택해야 합니다.")
                    LocalTime intakeTime,
            @Schema(description = "먹어야 하는 시간대", example = "[\"아침\", \"점심\", \"저녁\", \"취침전\"]")
                    @NotEmpty(message = "먹어야 하는 시간대는 하나 이상 선택해야 합니다.")
                    Set<String> timeCategories,
            @Schema(description = "메모", example = "의사 처방") String memo) {}

    @Schema(name = "MedicineHistoryRecordRequest", description = "복용 기록 요청 DTO")
    public record Record(
            @Schema(description = "복용 기록 시간", example = "2024-01-15T08:30:00")
                    @NotNull(message = "복용 기록 시간은 필수입니다.")
                    LocalDateTime recordedAt,
            @Schema(description = "기록 종류", example = "TAKEN") @NotNull(message = "기록 종류는 필수입니다.")
                    MedicineHistory.Status status,
            @Schema(description = "실제 복용량", example = "1.0") @NotNull(message = "복용량은 필수입니다.")
                    Double intakeAmount) {}
}
