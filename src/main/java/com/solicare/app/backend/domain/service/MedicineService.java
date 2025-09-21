package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.request.MedicineRequestDTO;
import com.solicare.app.backend.application.dto.res.MedicineResponseDTO;
import com.solicare.app.backend.application.mapper.MedicineMapper;
import com.solicare.app.backend.domain.dto.medicine.*;
import com.solicare.app.backend.domain.entity.Medicine;
import com.solicare.app.backend.domain.entity.Senior;
import com.solicare.app.backend.domain.repository.MedicineHistoryRepository;
import com.solicare.app.backend.domain.repository.MedicineRepository;
import com.solicare.app.backend.domain.repository.SeniorRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineService {
    private final SeniorRepository seniorRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineHistoryRepository medicineHistoryRepository;
    private final MedicineMapper medicineMapper;

    public MedicineCreateResult<MedicineResponseDTO.Info> createMedicine(
            String seniorUuid, MedicineRequestDTO.Create createDto) {
        try {
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("Senior not found"));

            return MedicineCreateResult.of(
                    MedicineCreateResult.Status.SUCCESS,
                    medicineMapper.toMedicineInfoDTO(
                            medicineRepository.save(
                                    medicineMapper.toEntity(createDto).linkSenior(senior))),
                    null);

        } catch (Exception e) {
            return MedicineCreateResult.of(MedicineCreateResult.Status.ERROR, null, e);
        }
    }

    @Transactional(readOnly = true)
    public MedicineQueryResult<MedicineResponseDTO.Info> getMedicines(String seniorUuid) {
        try {
            if (!seniorRepository.existsByUuid(seniorUuid)) {
                return MedicineQueryResult.of(
                        MedicineQueryResult.Status.NOT_FOUND,
                        null,
                        new IllegalArgumentException("Senior not found"));
            }

            List<MedicineResponseDTO.Info> infos =
                    medicineRepository.findBySenior_Uuid(seniorUuid).stream()
                            .map(medicineMapper::toMedicineInfoDTO)
                            .toList();

            return MedicineQueryResult.of(MedicineQueryResult.Status.SUCCESS, infos, null);
        } catch (Exception e) {
            return MedicineQueryResult.of(MedicineQueryResult.Status.ERROR, null, e);
        }
    }

    // TODO: implement deleteMedicine with cascade delete of medicine histories?

    public MedicineCreateResult<Void> deleteMedicine(String medicineUuid) {
        try {
            Medicine medicine =
                    medicineRepository
                            .findByUuid(medicineUuid)
                            .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

            // 관련 복용 기록들을 먼저 삭제
            medicineHistoryRepository.deleteAll(
                    medicineHistoryRepository.findByMedicine_Uuid(medicineUuid));

            // 약 삭제
            medicineRepository.delete(medicine);

            return MedicineCreateResult.of(MedicineCreateResult.Status.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            return MedicineCreateResult.of(MedicineCreateResult.Status.NOT_FOUND, null, e);
        } catch (Exception e) {
            return MedicineCreateResult.of(MedicineCreateResult.Status.ERROR, null, e);
        }
    }

    public MedicineCreateResult<MedicineResponseDTO.IntakeHistory> createMedicineHistory(
            String medicineUuid, MedicineRequestDTO.Record recordDto) {
        try {
            Medicine medicine =
                    medicineRepository
                            .findByUuid(medicineUuid)
                            .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
            return MedicineCreateResult.of(
                    MedicineCreateResult.Status.DELETE_SUCCESS,
                    medicineMapper.toMedicineHistoryDTO(
                            medicineHistoryRepository.save(
                                    medicineMapper.toEntity(recordDto, medicine))),
                    null);
        } catch (IllegalArgumentException e) {
            return MedicineCreateResult.of(MedicineCreateResult.Status.NOT_FOUND, null, e);
        } catch (Exception e) {
            return MedicineCreateResult.of(MedicineCreateResult.Status.ERROR, null, e);
        }
    }

    @Transactional(readOnly = true)
    public MedicineQueryResult<MedicineResponseDTO.IntakeHistory> getMedicineHistory(
            String medicineUuid) {
        try {
            if (!medicineRepository.existsByUuid(medicineUuid)) {
                return MedicineQueryResult.of(
                        MedicineQueryResult.Status.NOT_FOUND,
                        null,
                        new IllegalArgumentException("Medicine not found"));
            }
            List<MedicineResponseDTO.IntakeHistory> histories =
                    medicineHistoryRepository.findByMedicine_Uuid(medicineUuid).stream()
                            .map(medicineMapper::toMedicineHistoryDTO)
                            .toList();
            return MedicineQueryResult.of(MedicineQueryResult.Status.SUCCESS, histories, null);
        } catch (Exception e) {
            return MedicineQueryResult.of(MedicineQueryResult.Status.ERROR, null, e);
        }
    }

    @Transactional(readOnly = true)
    public MedicineQueryResult<MedicineResponseDTO.InfoWithHistory> getMedicineDetails(
            String seniorUuid, LocalDate date) {
        try {
            if (!seniorRepository.existsByUuid(seniorUuid)) {
                return MedicineQueryResult.of(
                        MedicineQueryResult.Status.NOT_FOUND,
                        null,
                        new IllegalArgumentException("Senior not found"));
            }
            List<MedicineResponseDTO.InfoWithHistory> medicineSummaries =
                    medicineRepository.findBySenior_Uuid(seniorUuid).stream()
                            .map(
                                    (medicine) -> {
                                        MedicineResponseDTO.Info medicineInfo =
                                                medicineMapper.toMedicineInfoDTO(medicine);

                                        List<MedicineResponseDTO.IntakeHistory> intakeHistories =
                                                medicineHistoryRepository
                                                        .findByMedicine_UuidAndRecordedAtAfter(
                                                                medicine.getUuid(),
                                                                date.atStartOfDay())
                                                        .stream()
                                                        .map(medicineMapper::toMedicineHistoryDTO)
                                                        .toList();

                                        return new MedicineResponseDTO.InfoWithHistory(
                                                medicineInfo, intakeHistories);
                                    })
                            .toList();
            return MedicineQueryResult.of(
                    MedicineQueryResult.Status.SUCCESS, medicineSummaries, null);
        } catch (Exception e) {
            return MedicineQueryResult.of(MedicineQueryResult.Status.ERROR, null, e);
        }
    }
}
