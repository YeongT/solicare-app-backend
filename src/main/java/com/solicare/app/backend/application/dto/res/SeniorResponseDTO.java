package com.solicare.app.backend.application.dto.res;

import com.solicare.app.backend.domain.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SeniorResponseDTO {
    @Schema(name = "SeniorLoginResponse", description = "시니어 로그인 응답 DTO")
    public record Login(
            @Schema(description = "로그인된 사용자 이름") String name,
            @Schema(description = "JWT 토큰") String token) {}

    @Schema(name = "SeniorProfileResponse", description = "시니어 정보 응답 DTO")
    public record Profile(
            @Schema(description = "UUID") String uuid,
            @Schema(description = "사용자 ID") String userId,
            @Schema(description = "이름") String name,
            @Schema(description = "나이") Integer age,
            @Schema(description = "성별") Gender gender,
            @Schema(description = "전화번호") String phoneNumber,
            @Schema(description = "주소") String address,
            @Schema(description = "비고") String note,
            @Schema(description = "모니터링 활성화 여부") Boolean monitored) {}
}
