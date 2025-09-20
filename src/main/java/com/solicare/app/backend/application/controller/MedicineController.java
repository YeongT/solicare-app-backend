package com.solicare.app.backend.application.controller;

import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.service.MedicineService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Medicine Management", description = "복용 스케줄 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/senior/{seniorUuid}/medicine")
@PreAuthorize("hasAnyRole('ADMIN','SENIOR')")
public class MedicineController {
    private final MedicineService medicineService;
    private final ApiResponseFactory apiResponseFactory;
}
