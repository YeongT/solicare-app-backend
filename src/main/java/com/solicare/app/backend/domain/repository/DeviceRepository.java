package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.Device;
import com.solicare.app.backend.domain.enums.PushMethod;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByUuid(String uuid);

    Optional<Device> findByPushMethodAndToken(PushMethod method, String token);

    List<Device> findByPushMethod(PushMethod method);

    List<Device> findByMember_Uuid(String memberUuid);

    List<Device> findBySenior_Uuid(String seniorUuid);

    boolean existsByPushMethodAndToken(PushMethod method, String token);

    void deleteByPushMethodAndToken(PushMethod method, String token);
}
