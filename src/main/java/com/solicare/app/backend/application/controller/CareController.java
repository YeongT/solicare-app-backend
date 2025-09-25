package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.dto.request.CareRequestDTO;
import com.solicare.app.backend.application.dto.request.MemberRequestDTO;
import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.BasicServiceResult;
import com.solicare.app.backend.domain.dto.care.CareLinkResult;
import com.solicare.app.backend.domain.dto.care.CareQueryResult;
import com.solicare.app.backend.domain.service.CareService;
import com.solicare.app.backend.global.auth.AuthUtil;
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

@Tag(name = "Care", description = "모니터링 및 케어 관련 API")
@RestController
@RequestMapping(path = "/api/care")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CareController {
    private final CareService careService;
    private final ApiResponseFactory apiResponseFactory;

    @Operation(
            summary = "모니터링 대상 목록 조회",
            description = "특정 회원의 UUID로, 해당 회원이 모니터링하는 시니어(모니터링 대상) 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    @GetMapping("/member/{memberUuid}/seniors")
    public ResponseEntity<ApiResponse<List<CareResponseDTO.SeniorBrief>>> getCareSeniors(
            Authentication authentication, @PathVariable String memberUuid) {
        if (AuthUtil.isDeniedToAccessMemberByMember(authentication, memberUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 대상 목록을 조회할 수 있습니다.");
        }
        CareQueryResult<List<CareResponseDTO.SeniorBrief>> result =
                careService.querySeniorByMember(memberUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "모니터링 대상 추가", description = "특정 회원의 UUID로, 해당 회원의 모니터링 대상(시니어)을 추가합니다.")
    @PostMapping("/member/{memberUuid}/seniors")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CareResponseDTO.SeniorBrief>> addCareSenior(
            Authentication authentication,
            @PathVariable String memberUuid,
            @RequestBody @Valid MemberRequestDTO.LinkSenior requestDto) {
        if (AuthUtil.isDeniedToAccessMemberByMember(authentication, memberUuid)) {
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
    @DeleteMapping("/member/{memberUuid}/seniors/{seniorUuid}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeCareSenior(
            Authentication authentication,
            @PathVariable String memberUuid,
            @PathVariable String seniorUuid) {
        if (AuthUtil.isDeniedToAccessMemberByMember(authentication, memberUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 대상을 삭제할 수 있습니다");
        }
        // TODO: implement removeCareSenior in CareService
        return apiResponseFactory.onFailure(ApiStatus._NOT_IMPLEMENTED, "Not implemented yet");
    }

    @Operation(
            summary = "보호자 목록 조회",
            description = "특정 시니어의 UUID로, 해당 시니어를 모니터링하는 보호자(멤버) 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    @GetMapping("/senior/{seniorUuid}/members")
    public ResponseEntity<ApiResponse<List<CareResponseDTO.MemberBrief>>> getCareMembers(
            Authentication authentication, @PathVariable String seniorUuid) {
        if (AuthUtil.isDeniedToAccessSeniorBySenior(authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 보호자 목록을 조회할 수 있습니다.");
        }
        CareQueryResult<List<CareResponseDTO.MemberBrief>> result =
                careService.queryMemberBySenior(seniorUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "보호자 추가", description = "특정 시니어의 UUID로, 해당 회원의 모니터링 보호자(멤버)를 추가합니다.")
    @PostMapping("/senior/{seniorUuid}/members")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CareResponseDTO.MemberBrief>> addCareMember(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @RequestBody @Valid SeniorRequestDTO.LinkMember requestDto) {
        if (AuthUtil.isDeniedToAccessSeniorBySenior(authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인만 자신의 보호자를 추가할 수 있습니다");
        }
        CareLinkResult<CareResponseDTO.MemberBrief> result =
                careService.linkMemberToSenior(seniorUuid, requestDto);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "보호자 삭제", description = "특정 시니어의 UUID로, 해당 회원의 특정 모니터링 보호자(멤버)를 삭제합니다.")
    @DeleteMapping("/senior/{seniorUuid}/members/{memberUuid}")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeCareMember(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @PathVariable String memberUuid) {
        if (AuthUtil.isDeniedToAccessSeniorBySenior(authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 대상을 삭제할 수 있습니다");
        }
        // TODO: implement removeCareMember in CareService
        return apiResponseFactory.onFailure(ApiStatus._NOT_IMPLEMENTED, "Not implemented yet");
    }

    @Operation(summary = "시니어 상세 조회", description = "특정 시니어의 UUID로 시니어 상세 정보를 조회합니다.")
    @GetMapping("/senior/{seniorUuid}")
    @PreAuthorize("hasAnyRole('MEMBER', 'SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CareResponseDTO.SeniorDetail>> getSeniorDetail(
            Authentication authentication, @PathVariable String seniorUuid) {
        if (AuthUtil.isDeniedToAccessSeniorByMemberOrSenior(
                careService, authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "해당 시니어의 상세 정보를 조회할 권한이 없습니다.");
        }
        CareQueryResult<CareResponseDTO.SeniorDetail> result =
                careService.getSeniorDetail(seniorUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(
            summary = "시니어 모니터링 상태 조회",
            description = "특정 시니어의 UUID로 시니어 모니터링 상태(on/off)를 조회합니다.")
    @GetMapping("/senior/{seniorUuid}/monitoring")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> getMonitoringEnabled(
            Authentication authentication, @PathVariable String seniorUuid) {
        if (AuthUtil.isDeniedToAccessSeniorBySenior(authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 모니터링 상태를 조회할 수 있습니다.");
        }
        BasicServiceResult<Boolean> result = careService.getMonitoringEnabled(seniorUuid);
        return result.getApiResponse(apiResponseFactory);
    }

    @Operation(summary = "시니어 모니터링 활성화/비활성화", description = "특정 시니어의 UUID로 모니터링 상태(on/off)를 변경합니다.")
    @PatchMapping("/senior/{seniorUuid}/monitoring")
    @PreAuthorize("hasAnyRole('MEMBER', 'SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateMonitoringEnabled(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @RequestParam("enabled") boolean enabled) {
        if (AuthUtil.isDeniedToAccessSeniorByMemberOrSenior(
                careService, authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "해당 시니어의 모니터링 상태를 변경할 권한이 없습니다.");
        }
        BasicServiceResult<Void> result = careService.setMonitoringEnabled(seniorUuid, enabled);
        return result.getApiResponse(apiResponseFactory);
    }

    @Operation(summary = "센서 데이터 등록", description = "특정 시니어의 UUID로 센서 데이터를 등록합니다.")
    @PostMapping("/senior/{seniorUuid}/stats")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CareResponseDTO.StatBrief>> addSensorStat(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @RequestBody @Valid CareRequestDTO.PostSensorStat dto) {
        if (AuthUtil.isDeniedToAccessSeniorBySenior(authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(
                    ApiStatus._FORBIDDEN, "본인만 자신의 센서 데이터를 등록할 수 있습니다.");
        }
        BasicServiceResult<CareResponseDTO.StatBrief> result =
                careService.addSensorStat(seniorUuid, dto);
        return result.getApiResponse(apiResponseFactory);
    }

    @Operation(summary = "알림 등록", description = "홈 서버로부터 이벤트를 수신하여 시니어의 알림을 생성하고 푸시로 전송합니다.")
    @PostMapping("/senior/{seniorUuid}/alerts")
    @PreAuthorize("hasAnyRole('SENIOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CareResponseDTO.AlertBrief>> addCareAlert(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @RequestBody @Valid CareRequestDTO.PostCareAlert requestDto) {
        if (AuthUtil.isDeniedToAccessSeniorBySenior(authentication, seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인만 자신의 알림을 등록할 수 있습니다.");
        }
        BasicServiceResult<CareResponseDTO.AlertBrief> result =
                careService.addCareAlert(seniorUuid, requestDto);
        return result.getApiResponse(apiResponseFactory);
    }
}
