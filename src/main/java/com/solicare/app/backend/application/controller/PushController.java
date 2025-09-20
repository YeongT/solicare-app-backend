package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.dto.request.PushRequestDTO;
import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.device.DeviceManageResult;
import com.solicare.app.backend.domain.dto.device.DeviceQueryResult;
import com.solicare.app.backend.domain.dto.push.PushBatchProcessResult;
import com.solicare.app.backend.domain.dto.push.PushDeliveryResult;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.service.DeviceService;
import com.solicare.app.backend.domain.service.PushService;
import com.solicare.app.backend.global.res.ApiResponse;
import com.solicare.app.backend.global.res.ApiStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Push", description = "디바이스 및 푸시 관련 API")
@RestController
@RequestMapping(path = "/api/push")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PushController {
    private final PushService pushService;
    private final DeviceService deviceService;
    private final ApiResponseFactory apiResponseFactory;

    @Operation(summary = "멤버 디바이스 목록 조회", description = "특정 회원의 UUID로, 해당 회원의 디바이스 목록을 조회합니다.")
    @GetMapping("/member/{memberUuid}/devices")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DeviceResponseDTO.Info>>> getMemberDevices(
            Authentication authentication, @PathVariable String memberUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(memberUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 디바이스 목록을 조회할 수 있습니다");
        }
        DeviceQueryResult result = deviceService.getDevices(Role.MEMBER, memberUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "멤버 디바이스 등록", description = "특정 멤버의 UUID로, 디바이스를 추가(연결)합니다.")
    @PutMapping("/member/{memberUuid}/devices/{deviceUuid}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO.Info>> linkDeviceToMember(
            Authentication authentication,
            @PathVariable String memberUuid,
            @PathVariable String deviceUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(memberUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인만 자신의 디바이스를 추가할 수 있습니다");
        }
        DeviceManageResult result = deviceService.link(Role.MEMBER, memberUuid, deviceUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "멤버 푸시 발송", description = "특정 멤버의 UUID로, 해당 회원의 모든 디바이스에 푸시 알림을 발송합니다.")
    @PostMapping("/member/{memberUuid}/push")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PushDeliveryResult>>> pushToMember(
            Authentication authentication,
            @PathVariable String memberUuid,
            @Valid @RequestBody PushRequestDTO.Send requestDTO) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(memberUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 디바이스에 푸시를 보낼 수 있습니다");
        }
        PushBatchProcessResult result =
                pushService.pushBatch(
                        Role.MEMBER,
                        memberUuid,
                        requestDTO.channel(),
                        requestDTO.title(),
                        requestDTO.message());
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getDetails(),
                null);
    }

    @Operation(
            summary = "시니어 디바이스 목록 조회",
            description = "특정 시니어의 UUID로, 해당 회원의 등록된 디바이스 목록을 조회합니다.")
    @GetMapping("/senior/{seniorUuid}/devices")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DeviceResponseDTO.Info>>> getSeniorDevices(
            Authentication authentication, @PathVariable String seniorUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 디바이스 목록을 조회할 수 있습니다");
        }
        DeviceQueryResult result = deviceService.getDevices(Role.SENIOR, seniorUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "시니어 디바이스 등록", description = "특정 시니어의 UUID로, 디바이스를 추가(연결)합니다.")
    @PutMapping("/senior/{seniorUuid}/devices/{deviceUuid}")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO.Info>> linkDeviceToSenior(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @PathVariable String deviceUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인만 자신의 디바이스를 추가할 수 있습니다");
        }
        DeviceManageResult result = deviceService.link(Role.SENIOR, seniorUuid, deviceUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "시니어 푸시 발송", description = "특정 시니어의 UUID로, 해당 회원의 모든 디바이스에 푸시 알림을 발송합니다.")
    @PostMapping("/senior/{seniorUuid}/push")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PushDeliveryResult>>> pushToSenior(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @Valid @RequestBody PushRequestDTO.Send requestDTO) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 디바이스에 푸시를 보낼 수 있습니다");
        }
        PushBatchProcessResult result =
                pushService.pushBatch(
                        Role.SENIOR,
                        seniorUuid,
                        requestDTO.channel(),
                        requestDTO.title(),
                        requestDTO.message());
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getDetails(),
                null);
    }
}
