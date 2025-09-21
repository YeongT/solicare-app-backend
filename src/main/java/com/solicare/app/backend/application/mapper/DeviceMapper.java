package com.solicare.app.backend.application.mapper;

import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.domain.entity.Device;

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
                device.getOwnerRole(),
                device.getOwnerUuid());
    }
}
