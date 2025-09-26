package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.Senior;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeniorRepository extends JpaRepository<Senior, String> {
    Optional<Senior> findByUuid(String uuid);

    Optional<Senior> findByUserId(String userid);

    boolean existsByUuid(String uuid);

    boolean existsByUserId(String userId);

    boolean existsByPhoneNumber(String phoneNumber);
}
