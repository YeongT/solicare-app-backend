package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.Device;
import com.solicare.app.backend.domain.enums.Push;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByUuid(String uuid);

    Optional<Device> findByTypeAndToken(Push type, String token);

    List<Device> findByType(Push type);

    List<Device> findByMember_Uuid(String memberUuid);

    List<Device> findBySenior_Uuid(String seniorUuid);

    boolean existsByTypeAndToken(Push type, String token);

    void deleteByTypeAndToken(Push type, String token);
}
