package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.dto.request.MedicineRequestDTO;
import com.solicare.app.backend.application.dto.res.MedicineResponseDTO;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.medicine.*;
import com.solicare.app.backend.domain.service.MedicineService;
import com.solicare.app.backend.global.res.ApiResponse;
import com.solicare.app.backend.global.res.ApiStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Medicine", description = "시니어 복용약 및 복용기록 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/senior/{seniorUuid}/medicine")
@PreAuthorize("hasAnyRole('ADMIN','SENIOR')")
public class MedicineController {
    private final MedicineService medicineService;
    private final ApiResponseFactory apiResponseFactory;

    @Operation(summary = "어르신 복용약 조회", description = "특정 UUID의 시니어가 복용해야 하는 모든 약 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO.Info>>> getMedicines(
            Authentication authentication, @PathVariable String seniorUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인의 약물 목록만 조회할 수 있습니다.");
        }

        MedicineQueryResult<MedicineResponseDTO.Info> result =
                medicineService.getMedicines(seniorUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "시니어 복용약 추가", description = "특정 UUID의 시니어가 복용해야 하는 새로운 약을 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponseDTO.Info>> createMedicine(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @RequestBody @Valid MedicineRequestDTO.Create createDto) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        MedicineCreateResult<MedicineResponseDTO.Info> result =
                medicineService.createMedicine(seniorUuid, createDto);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "약물 복용 기록 조회", description = "특정 약물의 모든 복용 기록을 조회합니다.")
    @GetMapping("/{medicineUuid}/history")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO.IntakeHistory>>> getMedicineHistory(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @PathVariable String medicineUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인의 복용 기록만 조회할 수 있습니다.");
        }

        MedicineQueryResult<MedicineResponseDTO.IntakeHistory> result =
                medicineService.getMedicineHistory(medicineUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "약물 복용 기록 추가", description = "특정 약물의 복용 기록을 추가합니다.")
    @PostMapping("/{medicineUuid}/history")
    public ResponseEntity<ApiResponse<MedicineResponseDTO.IntakeHistory>> createMedicineHistory(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @PathVariable String medicineUuid,
            @RequestBody @Valid MedicineRequestDTO.Record recordDto) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인의 복용 기록만 생성할 수 있습니다.");
        }

        MedicineCreateResult<MedicineResponseDTO.IntakeHistory> result =
                medicineService.createMedicineHistory(medicineUuid, recordDto);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(
            summary = "어르신 복용약 정보 및 복용 이력 조회",
            description = "특정 날짜 기준으로 어르신의 복용약 정보와 복용 이력을 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO.InfoWithHistory>>>
            getMedicineDetails(
                    Authentication authentication,
                    @PathVariable String seniorUuid,
                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인의 약물 상세 정보만 조회할 수 있습니다.");
        }

        MedicineQueryResult<MedicineResponseDTO.InfoWithHistory> result =
                medicineService.getMedicineDetails(seniorUuid, date);
        ApiStatus apiStatus =
                result.getStatus() == MedicineQueryResult.Status.SUCCESS
                        ? ApiStatus._OK
                        : ApiStatus._NOT_FOUND;
        return apiResponseFactory.onResult(
                apiStatus,
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }

    @Operation(summary = "시니어 복용약 삭제", description = "특정 UUID의 약과 관련된 모든 복용 기록을 삭제합니다.")
    @DeleteMapping("/{medicineUuid}")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(
            Authentication authentication,
            @PathVariable String seniorUuid,
            @PathVariable String medicineUuid) {
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !authentication.getName().equals(seniorUuid)) {
            return apiResponseFactory.onFailure(ApiStatus._FORBIDDEN, "본인의 약물만 삭제할 수 있습니다.");
        }

        MedicineCreateResult<Void> result = medicineService.deleteMedicine(medicineUuid);
        return apiResponseFactory.onResult(
                result.getStatus().getApiStatus(),
                result.getStatus().getCode(),
                result.getStatus().getMessage(),
                result.getResponse(),
                result.getException());
    }
}
