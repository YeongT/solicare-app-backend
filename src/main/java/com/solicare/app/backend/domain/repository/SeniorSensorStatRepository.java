package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.SeniorSensorStat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeniorSensorStatRepository extends JpaRepository<SeniorSensorStat, String> {
    Optional<SeniorSensorStat> findTopBySenior_UuidOrderByTimestampDesc(String seniorUuid);

    List<SeniorSensorStat> findTop20BySenior_UuidOrderByTimestampDesc(String seniorUuid);

    Slice<SeniorSensorStat> findBySenior_Uuid(String seniorUuid, Pageable pageable);

    Slice<SeniorSensorStat> findBySenior_UuidAndTimestampAfter(
            String seniorUuid, Pageable pageable, LocalDateTime after);
}
