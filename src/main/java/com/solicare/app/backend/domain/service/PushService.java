package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.application.enums.PushChannel;
import com.solicare.app.backend.application.mapper.DeviceMapper;
import com.solicare.app.backend.domain.dto.push.PushBatchProcessResult;
import com.solicare.app.backend.domain.dto.push.PushDeliveryResult;
import com.solicare.app.backend.domain.entity.Device;
import com.solicare.app.backend.domain.enums.PushMethod;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.repository.DeviceRepository;
import com.solicare.app.backend.domain.repository.MemberRepository;
import com.solicare.app.backend.domain.repository.SeniorRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PushService {
    private final FirebaseService firebaseService;
    private final MemberRepository memberRepository;
    private final SeniorRepository seniorRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public PushDeliveryResult sendPushToDevice(
            String deviceUuid, PushChannel channel, String title, String message) {
        Optional<Device> deviceOpt = deviceRepository.findByUuid(deviceUuid);
        if (deviceOpt.isEmpty()) {
            return PushDeliveryResult.of(
                    PushDeliveryResult.Status.UNAVAILABLE,
                    new IllegalArgumentException("Device not found"));
        }
        if (Objects.requireNonNull(deviceOpt.get().getPushMethod()) == PushMethod.FCM) {
            return firebaseService.sendMessageTo(
                    deviceOpt.get().getToken(), channel, title, message);
        }

        return PushDeliveryResult.of(
                PushDeliveryResult.Status.ERROR,
                new IllegalArgumentException(
                        "Unsupported push type: " + deviceOpt.get().getPushMethod()));
    }

    public PushBatchProcessResult pushBatch(
            Role role, String uuid, PushChannel channel, String title, String message) {
        if (!existsByRoleAndUuid(role, uuid)) {
            return PushBatchProcessResult.of(null, PushBatchProcessResult.Status.NOT_FOUND);
        }

        List<DeviceResponseDTO.Info> enabledDevices =
                switch (role) {
                    case MEMBER ->
                            deviceRepository.findByMember_Uuid(uuid).stream()
                                    .map(deviceMapper::from)
                                    .collect(Collectors.toList());
                    case SENIOR ->
                            deviceRepository.findBySenior_Uuid(uuid).stream()
                                    .map(deviceMapper::from)
                                    .collect(Collectors.toList());
                    default -> throw new IllegalArgumentException("Invalid role: " + role);
                };

        List<PushDeliveryResult> pushDeliveryResults =
                enabledDevices.stream()
                        .map(
                                device -> {
                                    if (Objects.requireNonNull(device.type()) == PushMethod.FCM) {
                                        return firebaseService.sendMessageTo(
                                                device.token(), channel, title, message);
                                    }
                                    return PushDeliveryResult.of(
                                            PushDeliveryResult.Status.ERROR,
                                            new IllegalArgumentException(
                                                    "Unsupported push type: " + device.type()));
                                })
                        .toList();
        return PushBatchProcessResult.of(pushDeliveryResults).setStatusByDetails();
    }

    // TODO: extract this method and remove duplicated code in Service classes
    private boolean existsByRoleAndUuid(Role role, String uuid) {
        return switch (role) {
            case MEMBER -> memberRepository.existsByUuid(uuid);
            case SENIOR -> seniorRepository.existsByUuid(uuid);
            default -> false;
        };
    }
}
