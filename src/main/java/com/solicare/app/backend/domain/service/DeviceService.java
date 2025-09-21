package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.application.enums.PushChannel;
import com.solicare.app.backend.application.mapper.DeviceMapper;
import com.solicare.app.backend.domain.dto.device.DeviceManageResult;
import com.solicare.app.backend.domain.dto.device.DeviceQueryResult;
import com.solicare.app.backend.domain.entity.Device;
import com.solicare.app.backend.domain.entity.Member;
import com.solicare.app.backend.domain.entity.Senior;
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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceService {
    private final PushService pushService;
    private final DeviceMapper deviceMapper;
    private final DeviceRepository deviceRepository;
    private final MemberRepository memberRepository;
    private final SeniorRepository seniorRepository;

    public boolean isDeviceOwner(
            Role role, String uuid, PushMethod pushMethodType, String deviceToken) {
        return getDevices(role, uuid).getResponse().stream()
                .anyMatch(
                        device ->
                                device.type() == pushMethodType
                                        && device.token().equals(deviceToken));
    }

    public DeviceQueryResult getDevices(Role role, String uuid) {
        switch (role) {
            case MEMBER -> {
                return DeviceQueryResult.of(
                        DeviceQueryResult.Status.SUCCESS,
                        deviceRepository.findByMember_Uuid(uuid).stream()
                                .map(deviceMapper::from)
                                .collect(Collectors.toList()),
                        null);
            }
            case SENIOR -> {
                return DeviceQueryResult.of(
                        DeviceQueryResult.Status.SUCCESS,
                        deviceRepository.findBySenior_Uuid(uuid).stream()
                                .map(deviceMapper::from)
                                .collect(Collectors.toList()),
                        null);
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    public DeviceQueryResult getAllDevicesByPush(PushMethod type) {
        return DeviceQueryResult.of(
                DeviceQueryResult.Status.SUCCESS,
                deviceRepository.findByPushMethod(type).stream()
                        .map(deviceMapper::from)
                        .collect(Collectors.toList()),
                null);
    }

    public DeviceQueryResult getCurrentStatus(PushMethod type, String token) {
        Device device = deviceRepository.findByPushMethodAndToken(type, token).orElse(null);
        if (device == null) {
            return DeviceQueryResult.of(
                    DeviceQueryResult.Status.ERROR,
                    null,
                    new IllegalArgumentException("TOKEN_NOT_FOUND"));
        }
        return DeviceQueryResult.of(
                DeviceQueryResult.Status.SUCCESS, List.of(deviceMapper.from(device)), null);
    }

    public DeviceManageResult update(PushMethod type, String oldToken, String newToken) {
        try {
            Device device = deviceRepository.findByPushMethodAndToken(type, oldToken).orElse(null);
            if (device == null) {
                return register(type, newToken);
            }

            if (deviceRepository.existsByPushMethodAndToken(type, newToken)) {
                return DeviceManageResult.of(DeviceManageResult.Status.ALREADY_EXISTS, null, null);
            }

            DeviceResponseDTO.Info info =
                    deviceMapper.from(deviceRepository.save(device.renew(newToken)));
            return DeviceManageResult.of(DeviceManageResult.Status.UPDATED, info, null);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return DeviceManageResult.of(
                        DeviceManageResult.Status.valueOf(e.getMessage()), null, e);
            }
            return DeviceManageResult.of(DeviceManageResult.Status.ERROR, null, e);
        }
    }

    public DeviceManageResult register(PushMethod method, String token) {
        try {
            if (deviceRepository.existsByPushMethodAndToken(method, token)) {
                return DeviceManageResult.of(DeviceManageResult.Status.ALREADY_EXISTS, null, null);
            }
            DeviceResponseDTO.Info info =
                    deviceMapper.from(
                            deviceRepository.save(
                                    Device.builder().pushMethod(method).token(token).build()));
            return DeviceManageResult.of(DeviceManageResult.Status.CREATED, info, null);
        } catch (Exception e) {
            return DeviceManageResult.of(DeviceManageResult.Status.ERROR, null, e);
        }
    }

    public DeviceManageResult delete(PushMethod type, String token) {
        try {
            Device device = deviceRepository.findByPushMethodAndToken(type, token).orElse(null);
            if (device == null) {
                return DeviceManageResult.of(
                        DeviceManageResult.Status.DEVICE_NOT_FOUND, null, null);
            }
            deviceRepository.delete(device);
            return DeviceManageResult.of(DeviceManageResult.Status.DELETED, null, null);
        } catch (Exception e) {
            return DeviceManageResult.of(DeviceManageResult.Status.ERROR, null, e);
        }
    }

    public DeviceManageResult link(Role role, String uuid, String deviceUuid) {
        try {
            Device device =
                    deviceRepository
                            .findByUuid(deviceUuid)
                            .orElseThrow(() -> new IllegalArgumentException("DEVICE_NOT_FOUND"));

            if ((device.getMember() != null && device.getMember().getUuid().equals(uuid))
                    || (device.getSenior() != null && device.getSenior().getUuid().equals(uuid))) {
                return DeviceManageResult.of(
                        DeviceManageResult.Status.ALREADY_LINKED, deviceMapper.from(device), null);
            }

            Member member =
                    (role == Role.MEMBER)
                            ? memberRepository
                                    .findByUuid(uuid)
                                    .orElseThrow(
                                            () -> new IllegalArgumentException("MEMBER_NOT_FOUND"))
                            : null;
            Senior senior =
                    (role == Role.SENIOR)
                            ? seniorRepository
                                    .findByUuid(uuid)
                                    .orElseThrow(
                                            () -> new IllegalArgumentException("SENIOR_NOT_FOUND"))
                            : null;
            pushService.pushBatch(role, uuid, PushChannel.INFO, "새로운 기기 연결", "새로운 기기가 연결되었습니다.");
            switch (role) {
                case MEMBER -> {
                    device.link(member);
                }
                case SENIOR -> {
                    device.link(senior);
                }
            }
            pushService.sendPushToDevice(
                    deviceUuid, PushChannel.INFO, "기기 연결 성공", "기기가 성공적으로 연결되었습니다.");
            return DeviceManageResult.of(
                    DeviceManageResult.Status.LINKED,
                    deviceMapper.from(deviceRepository.save(device)),
                    null);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return DeviceManageResult.of(
                        DeviceManageResult.Status.valueOf(e.getMessage()), null, e);
            }
            return DeviceManageResult.of(DeviceManageResult.Status.ERROR, null, e);
        }
    }

    public DeviceManageResult unlink(String deviceUuid) {
        try {
            Device device =
                    deviceRepository
                            .findByUuid(deviceUuid)
                            .orElseThrow(() -> new IllegalArgumentException("DEVICE_NOT_FOUND"));
            return DeviceManageResult.of(
                    DeviceManageResult.Status.UNLINKED,
                    deviceMapper.from(deviceRepository.save(device.unlink())),
                    null);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return DeviceManageResult.of(
                        DeviceManageResult.Status.valueOf(e.getMessage()), null, e);
            }
            return DeviceManageResult.of(DeviceManageResult.Status.ERROR, null, e);
        }
    }
}
