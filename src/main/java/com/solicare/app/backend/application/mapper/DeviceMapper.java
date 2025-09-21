package com.solicare.app.backend.application.mapper;

import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.domain.entity.Device;
import com.solicare.app.backend.domain.enums.Role;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceMapper {
    public DeviceResponseDTO.Info from(Device device) {
        return new DeviceResponseDTO.Info(
                device.getUuid(),
                device.getPushMethod(),
                device.getToken(),
                getOwnerRole(device),
                getOwnerUuid(device));
    }

    private Role getOwnerRole(Device device) {
        if (device.getMember() != null) {
            return Role.MEMBER;
        } else if (device.getSenior() != null) {
            return Role.SENIOR;
        } else {
            return null;
        }
    }

    private String getOwnerUuid(Device device) {
        if (device.getMember() != null) {
            return device.getMember().getUuid();
        } else if (device.getSenior() != null) {
            return device.getSenior().getUuid();
        } else {
            return null;
        }
    }
}
