package com.solicare.app.backend.application.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CareResponseDTO {
    @Schema(name = "CareMemberBriefResponse", description = "모니터링 보호자 목록 응답 DTO")
    public record MemberBrief(
            @Schema(description = "보호자 UUID") String uuid,
            @Schema(description = "보호자 이름") String name,
            @Schema(description = "보호자 이메일") String email,
            @Schema(description = "보호자 전화번호") String phoneNumber) {}

    @Schema(name = "CareSeniorBriefResponse", description = "모니터링 시니어 목록 응답 DTO")
    public record SeniorBrief(
            @Schema(description = "시니어 UUID") String uuid,
            @Schema(description = "시니어 이름") String name,
            @Schema(description = "읽지 않은 알림 수") Long unreadAlertCount) {}

    @Schema(name = "CareSeniorDetailResponse", description = "모니터링 시니어 상세 응답 DTO")
    public record SeniorDetail(@Schema(description = "시니어 정보") SeniorResponseDTO.Profile senior
            //            ,@Schema(description = "시니어의 최근 알림 이벤트") List<CareResponseDTO.Info> alerts
            //            ,@Schema(description = "시니어의 최근 건강 데이터") List<CareResponseDTO.Stat> stats
            ) {}
}
