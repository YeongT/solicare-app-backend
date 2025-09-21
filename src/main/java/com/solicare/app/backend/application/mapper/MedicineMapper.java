package com.solicare.app.backend.application.mapper;

import com.solicare.app.backend.application.dto.request.MedicineRequestDTO;
import com.solicare.app.backend.application.dto.res.MedicineResponseDTO;
import com.solicare.app.backend.application.enums.IntakeTime;
import com.solicare.app.backend.domain.entity.Medicine;
import com.solicare.app.backend.domain.entity.MedicineHistory;
import com.solicare.app.backend.global.utils.StringUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.Set;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineMapper {
    private String packDaysOfWeek(Set<DayOfWeek> days) {
        if (days.isEmpty()) return "";
        return StringUtils.packSetString(
                days.stream().map(DayOfWeek::name).collect(java.util.stream.Collectors.toSet()));
    }

    private Set<DayOfWeek> unpackDaysOfWeek(String str) {
        Set<String> dayNames = StringUtils.unpackSetString(str);
        return dayNames.stream()
                .map(DayOfWeek::valueOf)
                .collect(
                        java.util.stream.Collectors.toCollection(
                                () -> java.util.EnumSet.noneOf(DayOfWeek.class)));
    }

    private String packIntakeTimes(Set<IntakeTime> times) {
        if (times.isEmpty()) return "";
        return StringUtils.packSetString(
                times.stream().map(IntakeTime::name).collect(java.util.stream.Collectors.toSet()));
    }

    private Set<IntakeTime> unpackIntakeTimes(String str) {
        Set<String> timeNames = StringUtils.unpackSetString(str);
        return timeNames.stream()
                .map(IntakeTime::valueOf)
                .collect(
                        java.util.stream.Collectors.toCollection(
                                () -> java.util.EnumSet.noneOf(IntakeTime.class)));
    }

    public Medicine toEntity(MedicineRequestDTO.Create dto) {
        return Medicine.builder()
                .name(dto.name())
                .description(dto.description())
                .doseAmount(dto.doseAmount())
                .doseInstruction(dto.doseInstruction())
                .daysOfWeekStr(packDaysOfWeek(dto.daysOfWeek()))
                .intakeTimeStr(packIntakeTimes(dto.intakeTime()))
                .memo(dto.memo())
                .build();
    }

    public MedicineResponseDTO.Info toMedicineInfoDTO(Medicine medicine) {
        return new MedicineResponseDTO.Info(
                medicine.getUuid(),
                medicine.getName(),
                medicine.getDescription(),
                medicine.getDoseAmount(),
                medicine.getDoseInstruction(),
                unpackDaysOfWeek(medicine.getDaysOfWeekStr()),
                unpackIntakeTimes(medicine.getIntakeTimeStr()),
                medicine.getMemo());
    }

    public MedicineHistory toEntity(MedicineRequestDTO.Record dto, Medicine medicine) {
        return MedicineHistory.builder()
                .medicine(medicine)
                .recordedAt(dto.recordedAt())
                .status(dto.status())
                .intakeAmount(dto.intakeAmount())
                .build();
    }

    public MedicineResponseDTO.IntakeHistory toMedicineHistoryDTO(MedicineHistory history) {
        return new MedicineResponseDTO.IntakeHistory(
                history.getUuid(),
                history.getRecordedAt(),
                history.getIntakeAmount(),
                history.getStatus().name());
    }
}
