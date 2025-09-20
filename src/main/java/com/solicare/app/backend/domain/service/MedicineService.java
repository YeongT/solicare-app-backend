package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.request.MedicineRequestDTO;
import com.solicare.app.backend.application.dto.res.MedicineResponseDTO;
import com.solicare.app.backend.application.mapper.MedicineMapper;
import com.solicare.app.backend.domain.dto.medicine.MedicineCreateResult;
import com.solicare.app.backend.domain.dto.medicine.MedicineDetailQueryResult;
import com.solicare.app.backend.domain.dto.medicine.MedicineQueryResult;
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

    public MedicineCreateResult createMedicine(
            String seniorUuid, MedicineRequestDTO.Create createDto) {
        try {
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("Senior not found"));

            return MedicineCreateResult.of(
                    MedicineCreateResult.Status.SUCCESS,
                    medicineMapper.from(
                            medicineRepository.save(
                                    medicineMapper.toEntity(createDto).linkSenior(senior))),
                    null);

        } catch (Exception e) {
            return MedicineCreateResult.of(MedicineCreateResult.Status.ERROR, null, e);
        }
    }

    @Transactional(readOnly = true)
    public MedicineQueryResult getMedicines(String seniorUuid) {
        try {
            if (!seniorRepository.existsByUuid(seniorUuid)) {
                return MedicineQueryResult.of(
                        MedicineQueryResult.Status.NOT_FOUND,
                        null,
                        new IllegalArgumentException("Senior not found"));
            }

            List<MedicineResponseDTO.Info> infos =
                    medicineRepository.findBySenior_Uuid(seniorUuid).stream()
                            .map(medicineMapper::from)
                            .toList();

            return MedicineQueryResult.of(MedicineQueryResult.Status.SUCCESS, infos, null);
        } catch (Exception e) {
            return MedicineQueryResult.of(MedicineQueryResult.Status.ERROR, null, e);
        }
    }

    // TODO: implement deleteMedicine with cascade delete of medicine histories?

    // TODO: implement createMedicineHistory

    // TODO: implement getMedicineHistory

    @Transactional(readOnly = true)
    public MedicineDetailQueryResult getMedicineDetails(String seniorUuid, LocalDate date) {
        try {
            if (!seniorRepository.existsByUuid(seniorUuid)) {
                return MedicineDetailQueryResult.of(
                        MedicineDetailQueryResult.Status.NOT_FOUND,
                        null,
                        new IllegalArgumentException("Senior not found"));
            }

            List<MedicineResponseDTO.DetailedInfo> detailedInfos =
                    medicineRepository.findBySenior_Uuid(seniorUuid).stream()
                            .map(
                                    (medicine) -> {
                                        MedicineResponseDTO.Info medicineInfo =
                                                medicineMapper.from(medicine);

                                        List<MedicineResponseDTO.IntakeHistory> intakeHistories =
                                                medicineHistoryRepository
                                                        .findByMedicine_UuidAndRecordedAtAfter(
                                                                medicine.getUuid(),
                                                                date.atStartOfDay())
                                                        .stream()
                                                        .map(medicineMapper::from)
                                                        .toList();

                                        return new MedicineResponseDTO.DetailedInfo(
                                                medicineInfo, intakeHistories);
                                    })
                            .toList();

            return MedicineDetailQueryResult.of(
                    MedicineDetailQueryResult.Status.SUCCESS, detailedInfos, null);
        } catch (Exception e) {
            return MedicineDetailQueryResult.of(MedicineDetailQueryResult.Status.ERROR, null, e);
        }
    }
}
