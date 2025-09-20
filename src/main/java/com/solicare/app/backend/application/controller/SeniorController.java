package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.SeniorResponseDTO;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.senior.SeniorJoinResult;
import com.solicare.app.backend.domain.dto.senior.SeniorLoginResult;
import com.solicare.app.backend.domain.dto.senior.SeniorProfileResult;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Senior", description = "시니어(모니터링 대상) 관련 API")
@RestController
@RequestMapping("/api/senior")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SeniorController {
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
}
