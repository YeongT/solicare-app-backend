package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.dto.request.MemberRequestDTO;
import com.solicare.app.backend.application.dto.request.PushRequestDTO;
import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.application.dto.res.MemberResponseDTO;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.care.CareLinkResult;
import com.solicare.app.backend.domain.dto.care.CareQueryResult;
import com.solicare.app.backend.domain.dto.device.DeviceManageResult;
import com.solicare.app.backend.domain.dto.device.DeviceQueryResult;
import com.solicare.app.backend.domain.dto.member.MemberJoinResult;
import com.solicare.app.backend.domain.dto.member.MemberLoginResult;
import com.solicare.app.backend.domain.dto.member.MemberProfileResult;
import com.solicare.app.backend.domain.dto.push.PushBatchProcessResult;
import com.solicare.app.backend.domain.dto.push.PushDeliveryResult;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.service.CareService;
import com.solicare.app.backend.domain.service.DeviceService;
import com.solicare.app.backend.domain.service.MemberService;
import com.solicare.app.backend.domain.service.PushService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member", description = "멤버(모니터링 보호자) 관련 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberController {
    private final CareService careService;
    private final PushService pushService;
    private final DeviceService deviceService;
    private final MemberService memberService;
    private final ApiResponseFactory apiResponseFactory;

    @Operation(summary = "멤버 회원가입", description = "새로운 멤버를 등록합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "중복 회원")
    })
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<MemberResponseDTO.Login>> memberJoin(
            @RequestBody @Valid MemberRequestDTO.Join memberJoinRequestDTO) {
        MemberJoinResult result = memberService.createAndIssueToken(memberJoinRequestDTO);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "멤버 로그인", description = "멤버의 이메일 주소와 비밀번호로 로그인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "자격 증명 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberResponseDTO.Login>> memberLogin(
            @RequestBody @Valid MemberRequestDTO.Login memberLoginRequestDTO) {
        MemberLoginResult result = memberService.loginAndIssueToken(memberLoginRequestDTO);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "멤버 정보조회", description = "특정 멤버의 UUID로, 해당 회원의 회원정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "자격 증명 실패")
    })
    @GetMapping("/{memberUuid}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MemberResponseDTO.Profile>> getMemberProfile(
            Authentication authentication, @PathVariable String memberUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(memberUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인의 정보만 조회 가능합니다.");
        }
        MemberProfileResult result = memberService.getProfile(memberUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(
            summary = "모니터링 대상 목록 조회",
            description = "특정 회원의 UUID로, 해당 회원이 모니터링하는 시니어(모니터링 대상) 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    @GetMapping("/{memberUuid}/seniors")
    public ResponseEntity<ApiResponse<List<CareResponseDTO.SeniorBrief>>> getCareSeniors(
            Authentication authentication, @PathVariable String memberUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(memberUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 대상 목록을 조회할 수 있습니다.");
        }
        CareQueryResult<CareResponseDTO.SeniorBrief> result =
                careService.querySeniorByMember(memberUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "모니터링 대상 추가", description = "특정 회원의 UUID로, 해당 회원의 모니터링 대상(시니어)을 추가합니다.")
    @PostMapping("/{memberUuid}/seniors")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CareResponseDTO.SeniorBrief>> addCareSenior(
            Authentication authentication,
            @PathVariable String memberUuid,
            @RequestBody @Valid MemberRequestDTO.LinkSenior requestDto) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(memberUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 대상을 추가할 수 있습니다");
        }
        CareLinkResult<CareResponseDTO.SeniorBrief> result =
                careService.linkSeniorToMember(memberUuid, requestDto);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "모니터링 대상 삭제", description = "특정 회원의 UUID로, 해당 회원의 특정 모니터링 대상(시니어)을 삭제합니다.")
    @DeleteMapping("/{memberUuid}/seniors/{seniorUuid}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeCareSenior(
            Authentication authentication,
            @PathVariable String memberUuid,
            @PathVariable String seniorUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(memberUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 대상을 삭제할 수 있습니다");
        }
        // TODO: implement removeCareSenior in CareService
        return apiResponseFactory.onFailure(ApiStatus._NOT_IMPLEMENTED, "Not implemented yet");
    }

    @Operation(summary = "멤버 디바이스 목록 조회", description = "특정 회원의 UUID로, 해당 회원의 디바이스 목록을 조회합니다.")
    @GetMapping("/{memberUuid}/devices")
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
    @PutMapping("/{memberUuid}/devices/{deviceUuid}")
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
    @PostMapping("/{memberUuid}/push")
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
}
