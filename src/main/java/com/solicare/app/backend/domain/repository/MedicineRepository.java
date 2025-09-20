package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.Medicine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, String> {
    Optional<Medicine> findByUuid(String uuid);

    List<Medicine> findBySenior_Uuid(String seniorUuid);

    boolean existsByUuid(String uuid);
}
