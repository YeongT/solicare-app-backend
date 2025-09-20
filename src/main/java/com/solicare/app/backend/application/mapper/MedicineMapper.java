package com.solicare.app.backend.application.mapper;

import com.solicare.app.backend.application.dto.request.MedicineRequestDTO;
import com.solicare.app.backend.application.dto.res.MedicineResponseDTO;
import com.solicare.app.backend.domain.entity.Medicine;
import com.solicare.app.backend.domain.entity.MedicineHistory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineMapper {

    public Medicine toEntity(MedicineRequestDTO.Create dto) {
        return Medicine.builder()
                .name(dto.name())
                .description(dto.description())
                .doseAmount(dto.doseAmount())
                .doseInstruction(dto.doseInstruction())
                .daysOfWeek(dto.daysOfWeek())
                .memo(dto.memo())
                .intakeTime(dto.intakeTime())
                .timeCategories(dto.timeCategories())
                .build();
    }

    public MedicineResponseDTO.Info from(Medicine medicine) {
        return new MedicineResponseDTO.Info(
                medicine.getName(),
                medicine.getDescription(),
                medicine.getDoseAmount(),
                medicine.getDoseInstruction(),
                medicine.getDaysOfWeek(),
                medicine.getIntakeTime(),
                medicine.getMemo(),
                medicine.getTimeCategories());
    }

    public MedicineHistory toEntity(MedicineRequestDTO.Record dto, Medicine medicine) {
        return MedicineHistory.builder()
                .medicine(medicine)
                .recordedAt(dto.recordedAt())
                .status(dto.status())
                .intakeAmount(dto.intakeAmount())
                .build();
    }

    public MedicineResponseDTO.IntakeHistory from(MedicineHistory history) {
        return new MedicineResponseDTO.IntakeHistory(
                history.getUuid(),
                history.getRecordedAt(),
                history.getIntakeAmount(),
                history.getStatus().name());
    }
}
