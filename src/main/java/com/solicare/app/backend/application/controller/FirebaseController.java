package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.dto.request.PushRequestDTO;
import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.BasicServiceResult;
import com.solicare.app.backend.domain.dto.device.DeviceManageResult;
import com.solicare.app.backend.domain.dto.push.PushDeliveryResult;
import com.solicare.app.backend.domain.enums.PushMethod;
import com.solicare.app.backend.domain.service.DeviceService;
import com.solicare.app.backend.domain.service.FirebaseService;
import com.solicare.app.backend.global.res.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Firebase", description = "Google Firebase 관련 API")
@RestController
@RequestMapping("/api/firebase")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseController {
    private final DeviceService deviceService;
    private final FirebaseService firebaseService;
    private final ApiResponseFactory apiResponseFactory;

    @Operation(summary = "FCM 등록된 기기 목록 조회", description = "(관리자) 현재 등록된 모든 FCM 기기 목록을 조회합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/fcm/devices")
    public ResponseEntity<ApiResponse<List<DeviceResponseDTO.Info>>> fcmDevices() {
        BasicServiceResult<List<DeviceResponseDTO.Info>> result =
                deviceService.getAllDevicesByPush(PushMethod.FCM);
        return result.getApiResponse(apiResponseFactory);
    }

    @Operation(summary = "FCM 상태 확인", description = "FCM 토큰의 등록 상태를 확인합니다.")
    @PreAuthorize("hasAnyRole('SENIOR', 'MEMBER', 'ADMIN')")
    @GetMapping("/fcm/status")
    public ResponseEntity<ApiResponse<DeviceResponseDTO.Info>> fcmStatus(
            @RequestParam String token) {
        BasicServiceResult<DeviceResponseDTO.Info> result =
                deviceService.getDeviceInfo(PushMethod.FCM, token);
        return result.getApiResponse(apiResponseFactory);
    }

    @Operation(summary = "FCM 토큰 등록", description = "FCM 토큰을 등록합니다.")
    @PostMapping("/fcm/register")
    public ResponseEntity<ApiResponse<DeviceResponseDTO.Info>> fcmRegister(
            @Valid @RequestBody PushRequestDTO.TokenBody dto) {
        DeviceManageResult result = deviceService.register(PushMethod.FCM, dto.token());
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "FCM 푸시 전송", description = "특정 토큰으로 FCM 푸시를 전송합니다.")
    @PreAuthorize("hasAnyRole('SENIOR', 'MEMBER', 'ADMIN')")
    @PostMapping("/fcm/push")
    public ResponseEntity<ApiResponse<Void>> fcmPush(@RequestBody @Valid PushRequestDTO.Send dto) {
        PushDeliveryResult result =
                firebaseService.sendMessageTo(
                        dto.token(), dto.channel(), dto.title(), dto.message());
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                null,
                result.getException());
    }

    @Operation(summary = "FCM 토큰 삭제", description = "특정 FCM 토큰을 DB에서 삭제합니다.")
    @DeleteMapping("/fcm/{token}")
    public ResponseEntity<ApiResponse<DeviceResponseDTO.Info>> fcmUnregister(
            @PathVariable String token) {
        DeviceManageResult result = deviceService.delete(PushMethod.FCM, token);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(
            summary = "FCM 토큰 갱신",
            description = "기존 FCM 토큰을 새로운 토큰으로 업데이트 합니다. 기존에 등록된 토큰이 없으면 새로 등록합니다.")
    @PutMapping("/fcm/renew/{oldToken}")
    public ResponseEntity<ApiResponse<DeviceResponseDTO.Info>> fcmRenew(
            @PathVariable String oldToken, @RequestBody @Valid PushRequestDTO.TokenBody dto) {
        DeviceManageResult result = deviceService.update(PushMethod.FCM, oldToken, dto.token());
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }
}
