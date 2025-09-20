package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.MedicineHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicineHistoryRepository extends JpaRepository<MedicineHistory, Long> {
    List<MedicineHistory> findByMedicine_Uuid(String medicineUuid);

    List<MedicineHistory> findByMedicine_UuidAndRecordedAtAfter(
            String medicine_uuid, LocalDateTime recordedAt);
}
