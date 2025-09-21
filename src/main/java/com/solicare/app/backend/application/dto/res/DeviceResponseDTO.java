package com.solicare.app.backend.application.dto.res;

import com.solicare.app.backend.domain.enums.PushMethod;
import com.solicare.app.backend.domain.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeviceResponseDTO {
    @Schema(name = "DeviceInfoResponse", description = "디바이스 정보 응답 DTO")
    public record Info(
            @Schema(description = "디바이스 UUID") String uuid,
            @Schema(description = "디바이스 푸시 타입") PushMethod type,
            @Schema(description = "디바이스 푸시 토큰") String token,
            @Schema(description = "디바이스 소유주 Role") Role ownerRole,
            @Schema(description = "디바이스 소유주 UUID") String ownerUuid) {}
}
