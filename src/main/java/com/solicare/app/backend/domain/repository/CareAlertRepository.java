package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.CareAlert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareAlertRepository extends JpaRepository<CareAlert, String> {
    Optional<CareAlert> findByUuid(String uuid);

    List<CareAlert> findTop5BySenior_UuidAndIsDismissedIsFalseOrderByTimestampDesc(
            String seniorUuid);

    Page<CareAlert> findAllBySenior_Uuid(String seniorUuid, Pageable pageable);

    Page<CareAlert> findBySenior_UuidAndIsDismissedIsFalse(String seniorUuid, Pageable pageable);

    long countBySenior_UuidAndIsReadFalseAndIsDismissedIsFalse(String seniorUuid);
}
