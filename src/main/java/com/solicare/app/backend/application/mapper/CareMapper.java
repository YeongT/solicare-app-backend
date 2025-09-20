package com.solicare.app.backend.application.mapper;

import com.solicare.app.backend.application.dto.request.CareRequestDTO.PostCareAlert;
import com.solicare.app.backend.application.dto.request.CareRequestDTO.PostSensorStat;
import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.domain.entity.CareAlert;
import com.solicare.app.backend.domain.entity.Member;
import com.solicare.app.backend.domain.entity.Senior;
import com.solicare.app.backend.domain.entity.SeniorSensorStat;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CareMapper {
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public CareResponseDTO.MemberBrief toMemberBriefDTO(Member member) {
        return new CareResponseDTO.MemberBrief(
                member.getUuid(), member.getName(), member.getEmail(), member.getPhoneNumber());
    }

    public CareResponseDTO.SeniorBrief toSeniorBriefDTO(Senior senior, Long unreadAlertCount) {
        return new CareResponseDTO.SeniorBrief(
                senior.getUuid(), senior.getName(), unreadAlertCount);
    }

    public SeniorSensorStat toEntity(PostSensorStat dto, Senior senior) {
        return SeniorSensorStat.builder()
                .senior(senior)
                .timestamp(dto.timestamp())
                .cameraFallDetected(dto.cameraFallDetected())
                .wearableFallDetected(dto.wearableFallDetected())
                .temperature(dto.temperature())
                .humidity(dto.humidity())
                .heartRate(dto.heartRate())
                .wearableBattery(dto.wearableBattery())
                .build();
    }

    public CareAlert toEntity(PostCareAlert dto, Senior senior) {
        return CareAlert.builder()
                .senior(senior)
                .timestamp(dto.timestamp())
                .eventType(dto.eventType())
                .monitorMode(dto.monitorMode())
                .base64Image(dto.base64Image())
                .isRead(Boolean.TRUE.equals(dto.isRead()))
                .isDismissed(Boolean.TRUE.equals(dto.isDismissed()))
                .build();
    }

    public CareResponseDTO.AlertBrief toAlertBrief(CareAlert alert) {
        return new CareResponseDTO.AlertBrief(
                alert.getUuid(),
                alert.getEventType().name(),
                alert.getTimestamp().format(ISO_FORMAT),
                alert.getIsRead());
    }

    public CareResponseDTO.AlertDetail toAlertDetail(CareAlert alert) {
        return new CareResponseDTO.AlertDetail(
                alert.getUuid(),
                alert.getEventType().name(),
                alert.getMonitorMode().name(),
                alert.getTimestamp().format(ISO_FORMAT),
                alert.getBase64Image(),
                alert.getIsRead(),
                alert.getIsDismissed());
    }

    public CareResponseDTO.StatBrief toStatBrief(SeniorSensorStat stat) {
        return new CareResponseDTO.StatBrief(
                stat.getUuid(),
                stat.getTimestamp().format(ISO_FORMAT),
                stat.getHeartRate(),
                stat.getTemperature());
    }

    public CareResponseDTO.StatDetail toStatDetail(SeniorSensorStat stat) {
        return new CareResponseDTO.StatDetail(
                stat.getUuid(),
                stat.getTimestamp().format(ISO_FORMAT),
                stat.getCameraFallDetected(),
                stat.getWearableFallDetected(),
                stat.getTemperature(),
                stat.getHumidity(),
                stat.getHeartRate(),
                stat.getWearableBattery());
    }
}
