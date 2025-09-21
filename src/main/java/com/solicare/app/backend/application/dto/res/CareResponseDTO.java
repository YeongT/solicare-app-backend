package com.solicare.app.backend.application.dto.res;

import com.solicare.app.backend.domain.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

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
            @Schema(description = "시니어 나이") Integer age,
            @Schema(description = "시니어 성별") Gender gender,
            @Schema(description = "읽지 않은 알림 수") Long unreadAlertCount) {}

    @Schema(name = "CareSeniorDetailResponse", description = "모니터링 시니어 상세 응답 DTO")
    public record SeniorDetail(
            @Schema(description = "시니어 프로필 정보") SeniorResponseDTO.Profile profile,
            @Schema(description = "시니어 모니터링 활성화 여부") Boolean isMonitored,
            @Schema(description = "시니어의 최근 알림 이벤트") List<CareResponseDTO.AlertBrief> alerts,
            @Schema(description = "시니어의 최근 건강 데이터") List<CareResponseDTO.StatBrief> stats) {}

    @Schema(name = "AlertBrief", description = "알림 요약 응답 DTO")
    public record AlertBrief(
            @Schema(description = "알림 UUID") String uuid,
            @Schema(description = "알림 이벤트 타입") String eventType,
            @Schema(description = "알림 발생 시각") String timestamp,
            @Schema(description = "알림 읽음 여부") Boolean isRead) {}

    @Schema(name = "StatBrief", description = "센서 통계 요약 응답 DTO")
    public record StatBrief(
            @Schema(description = "기록 UUID") String uuid,
            @Schema(description = "기록 시각") String timestamp,
            @Schema(description = "평균 심박수") Integer heartRate,
            @Schema(description = "평균 온도") Double temperature) {}

    @Schema(name = "AlertDetail", description = "알림 상세 응답 DTO")
    public record AlertDetail(
            @Schema(description = "알림 UUID") String uuid,
            @Schema(description = "이벤트 타입") String eventType,
            @Schema(description = "모니터링 모드") String monitorMode,
            @Schema(description = "발생 시각") String timestamp,
            @Schema(description = "이미지(Base64)") String base64Image,
            @Schema(description = "알림 읽음 여부") Boolean isRead,
            @Schema(description = "알림 무시 여부") Boolean isDismissed) {}

    @Schema(name = "StatDetail", description = "센서 통계 상세 응답 DTO")
    public record StatDetail(
            @Schema(description = "기록 UUID") String uuid,
            @Schema(description = "측정 시각") String timestamp,
            @Schema(description = "카메라 낙상 감지 여부") Boolean cameraFallDetected,
            @Schema(description = "웨어러블 낙상 감지 여부") Boolean wearableFallDetected,
            @Schema(description = "체온") Double temperature,
            @Schema(description = "습도") Double humidity,
            @Schema(description = "심박수") Integer heartRate,
            @Schema(description = "웨어러블 배터리 잔량(%)") Double wearableBattery) {}
}
