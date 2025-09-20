package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.dto.request.PushRequestDTO;
import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.application.dto.res.SeniorResponseDTO;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.BasicServiceResult;
import com.solicare.app.backend.domain.dto.care.CareQueryResult;
import com.solicare.app.backend.domain.dto.device.DeviceManageResult;
import com.solicare.app.backend.domain.dto.device.DeviceQueryResult;
import com.solicare.app.backend.domain.dto.push.PushBatchProcessResult;
import com.solicare.app.backend.domain.dto.push.PushDeliveryResult;
import com.solicare.app.backend.domain.dto.senior.SeniorJoinResult;
import com.solicare.app.backend.domain.dto.senior.SeniorLoginResult;
import com.solicare.app.backend.domain.dto.senior.SeniorProfileResult;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.service.CareService;
import com.solicare.app.backend.domain.service.DeviceService;
import com.solicare.app.backend.domain.service.PushService;
import com.solicare.app.backend.domain.service.SeniorService;
import com.solicare.app.backend.global.res.ApiResponse;
import com.solicare.app.backend.global.res.ApiStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Senior", description = "시니어(모니터링 대상) 관련 API")
@RestController
@RequestMapping("/api/senior")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SeniorController {
    private final CareService careService;
    private final PushService pushService;
    private final DeviceService deviceService;
    private final SeniorService seniorService;
    private final ApiResponseFactory apiResponseFactory;

    @Operation(summary = "시니어 회원가입", description = "새로운 시니어를 등록합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "이미 존재하는 모니터링 대상 사용자 ID")
    })
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<SeniorResponseDTO.Login>> seniorJoin(
            @RequestBody @Valid SeniorRequestDTO.Join seniorJoinRequestDTO) {
        SeniorJoinResult result = seniorService.createAndIssueToken(seniorJoinRequestDTO);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "시니어 로그인", description = "시니어의 사용자 ID와 PW로 로그인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "자격 증명 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SeniorResponseDTO.Login>> seniorLogin(
            @RequestBody @Valid SeniorRequestDTO.Login seniorLoginRequestDTO) {
        SeniorLoginResult result = seniorService.loginAndIssueToken(seniorLoginRequestDTO);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "시니어 정보조회", description = "특정 시니어의 UUID로, 해당 시니어의 상세 프로필 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "자격 증명 실패")
    })
    @GetMapping("/{seniorUuid}")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<SeniorResponseDTO.Profile>> getSeniorProfile(
            Authentication authentication, @PathVariable String seniorUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인의 정보만 조회 가능합니다.");
        }
        SeniorProfileResult result = seniorService.getProfile(seniorUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(
            summary = "보호자 목록 조회",
            description = "특정 시니어의 UUID로, 해당 시니어를 모니터링하는 보호자(멤버) 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    @GetMapping("/{seniorUuid}/members")
    public ResponseEntity<ApiResponse<List<CareResponseDTO.MemberBrief>>> getCareMembers(
            Authentication authentication, @PathVariable String seniorUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 보호자 목록을 조회할 수 있습니다.");
        }
        CareQueryResult<CareResponseDTO.MemberBrief> result =
                careService.queryMemberBySenior(seniorUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "보호자 삭제", description = "특정 시니어의 UUID로, 해당 회원의 특정 모니터링 보호자(멤버)를 삭제합니다.")
    @DeleteMapping("/{seniorUuid}/members/{memberUuid}")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeCareMember(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @PathVariable String memberUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 대상을 삭제할 수 있습니다");
        }
        // TODO: implement removeCareMember in CareService
        return apiResponseFactory.onFailure(ApiStatus._NOT_IMPLEMENTED, "Not implemented yet");
    }

    @Operation(
            summary = "시니어 디바이스 목록 조회",
            description = "특정 시니어의 UUID로, 해당 회원의 등록된 디바이스 목록을 조회합니다.")
    @GetMapping("/{seniorUuid}/devices")
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
    @PutMapping("/{seniorUuid}/devices/{deviceUuid}")
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
    @PostMapping("/{seniorUuid}/push")
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

    @Operation(summary = "시니어 모니터링 활성화/비활성화", description = "특정 시니어의 UUID로 모니터링 상태(on/off)를 변경합니다.")
    @PatchMapping("/{seniorUuid}/monitoring")
    @PreAuthorize("hasAnyRole('MEMBER', 'SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<SeniorResponseDTO.Profile>> updateMonitoringEnabled(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @RequestParam("enabled") boolean enabled) {
        Set<String> authorities =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
        boolean isAdmin = authorities.contains("ROLE_ADMIN");
        boolean isMember = authorities.contains("ROLE_MEMBER");
        boolean isSenior = authorities.contains("ROLE_SENIOR");
        if (!isAdmin) {
            if (isSenior && !authentication.getName().equals(seniorUuid)) {
                return apiResponseFactory.onFailure(
                        ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 상태를 변경할 수 있습니다.");
            }
            if (isMember) {
                BasicServiceResult<Boolean> careCheckResult =
                        careService.hasMemberAccessToSenior(authentication.getName(), seniorUuid);
                if (!careCheckResult.getPayload()) {
                    return careCheckResult.getException() != null
                            ? apiResponseFactory.onResult(
                                    ApiStatus._INTERNAL_SERVER_ERROR,
                                    ApiStatus._INTERNAL_SERVER_ERROR.getCode(),
                                    ApiStatus._INTERNAL_SERVER_ERROR.getMessage(),
                                    null,
                                    careCheckResult.getException())
                            : apiResponseFactory.onFailure(
                                    ApiStatus._FORBIDDEN, "해당 시니어의 모니터링 상태를 변경할 권한이 없습니다.");
                }
            }
        }
        BasicServiceResult<SeniorResponseDTO.Profile> result =
                seniorService.setMonitoringEnabled(seniorUuid, enabled);
        return result.getApiResponse(apiResponseFactory);
    }
}
