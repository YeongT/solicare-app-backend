package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.request.CareRequestDTO.PostCareAlert;
import com.solicare.app.backend.application.dto.request.CareRequestDTO.PostSensorStat;
import com.solicare.app.backend.application.dto.request.MemberRequestDTO;
import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.application.dto.res.SeniorResponseDTO;
import com.solicare.app.backend.application.enums.PushChannel;
import com.solicare.app.backend.application.enums.SeniorEvent;
import com.solicare.app.backend.application.mapper.CareMapper;
import com.solicare.app.backend.application.mapper.SeniorMapper;
import com.solicare.app.backend.domain.dto.BasicServiceResult;
import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.domain.dto.care.CareLinkResult;
import com.solicare.app.backend.domain.dto.care.CareQueryResult;
import com.solicare.app.backend.domain.entity.*;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.repository.CareAlertRepository;
import com.solicare.app.backend.domain.repository.CareRelationRepository;
import com.solicare.app.backend.domain.repository.MemberRepository;
import com.solicare.app.backend.domain.repository.SeniorRepository;
import com.solicare.app.backend.domain.repository.SeniorSensorStatRepository;

import jakarta.transaction.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CareService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final SeniorRepository seniorRepository;
    private final CareAlertRepository careAlertRepository;
    private final CareRelationRepository careRelationRepository;
    private final SeniorSensorStatRepository seniorSensorStatRepository;
    private final CareMapper careMapper;
    private final SeniorMapper seniorMapper;
    private final PushService pushService;

    public BasicServiceResult<Boolean> hasMemberAccessToSenior(
            String memberUuid, String seniorUuid) {
        try {
            Member member =
                    memberRepository
                            .findByUuid(memberUuid)
                            .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));
            boolean linked = careRelationRepository.existsByMemberAndSenior(member, senior);
            return BasicServiceResult.of(ServiceResult.GenericStatus.SUCCESS, linked, null);
        } catch (Exception e) {
            return BasicServiceResult.of(ServiceResult.GenericStatus.ERROR, null, e);
        }
    }

    public CareQueryResult<List<CareResponseDTO.SeniorBrief>> querySeniorByMember(
            String memberUuid) {
        try {
            Member member =
                    memberRepository
                            .findByUuid(memberUuid)
                            .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
            List<CareResponseDTO.SeniorBrief> seniorBriefs =
                    careRelationRepository.findByMemberOrderBySenior_NameAsc(member).stream()
                            .map(
                                    care -> {
                                        Senior senior = care.getSenior();
                                        long unreadAlertCount =
                                                careAlertRepository
                                                        .countBySenior_UuidAndIsReadFalseAndIsDismissedIsFalse(
                                                                senior.getUuid());
                                        return careMapper.toSeniorBriefDTO(
                                                senior, unreadAlertCount);
                                    })
                            .toList();
            return CareQueryResult.of(CareQueryResult.Status.SUCCESS, seniorBriefs, null);
        } catch (Exception e) {
            return CareQueryResult.of(CareQueryResult.Status.ERROR, null, e);
        }
    }

    public CareQueryResult<List<CareResponseDTO.MemberBrief>> queryMemberBySenior(
            String seniorUuid) {
        try {
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));
            List<Member> members =
                    careRelationRepository.findBySeniorOrderByMember_NameAsc(senior).stream()
                            .map(Care::getMember)
                            .toList();
            return CareQueryResult.of(
                    CareQueryResult.Status.SUCCESS,
                    members.stream().map(careMapper::toMemberBriefDTO).toList(),
                    null);
        } catch (Exception e) {
            return CareQueryResult.of(CareQueryResult.Status.ERROR, null, e);
        }
    }

    public CareLinkResult<CareResponseDTO.SeniorBrief> linkSeniorToMember(
            String memberUuid, MemberRequestDTO.LinkSenior linkDto) {
        try {
            Member member =
                    memberRepository
                            .findByUuid(memberUuid)
                            .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
            Senior senior =
                    seniorRepository
                            .findByUserId(linkDto.userId())
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));

            if (careRelationRepository.existsByMemberAndSenior(member, senior)) {
                return CareLinkResult.of(CareLinkResult.Status.ALREADY_LINKED, null, null);
            }

            if (!passwordEncoder.matches(linkDto.password(), senior.getPassword())) {
                return CareLinkResult.of(CareLinkResult.Status.INVALID_SENIOR_PASSWORD, null, null);
            }
            careRelationRepository.save(Care.builder().member(member).senior(senior).build());

            pushService.pushBatch(
                    Role.MEMBER,
                    memberUuid,
                    PushChannel.INFO,
                    "새로운 모니터링 대상",
                    String.format("모니터링 대상 %s님에 의해 보호자로 추가되었습니다.", senior.getName()),
                    Optional.empty());
            return CareLinkResult.of(
                    CareLinkResult.Status.SUCCESS, careMapper.toSeniorBriefDTO(senior, 0L), null);
        } catch (Exception e) {
            return CareLinkResult.of(CareLinkResult.Status.ERROR, null, e);
        }
    }

    public CareLinkResult<CareResponseDTO.MemberBrief> linkMemberToSenior(
            String seniorUuid, SeniorRequestDTO.LinkMember linkDto) {
        try {
            Member member =
                    memberRepository
                            .findByEmail(linkDto.email())
                            .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));

            if (careRelationRepository.existsByMemberAndSenior(member, senior)) {
                return CareLinkResult.of(CareLinkResult.Status.ALREADY_LINKED, null, null);
            }

            if (!passwordEncoder.matches(linkDto.password(), member.getPassword())) {
                return CareLinkResult.of(CareLinkResult.Status.INVALID_MEMBER_PASSWORD, null, null);
            }
            careRelationRepository.save(Care.builder().member(member).senior(senior).build());
            pushService.pushBatch(
                    Role.SENIOR,
                    senior.getUuid(),
                    PushChannel.INFO,
                    "새로운 보호자 등록",
                    String.format("새로운 보호자 %s가 모니터링을 시작했습니다.", member.getName()),
                    Optional.empty());
            return CareLinkResult.of(
                    CareLinkResult.Status.SUCCESS, careMapper.toMemberBriefDTO(member), null);
        } catch (Exception e) {
            return CareLinkResult.of(CareLinkResult.Status.ERROR, null, e);
        }
    }

    public BasicServiceResult<Boolean> getMonitoringEnabled(String seniorUuid) {
        Senior senior = seniorRepository.findByUuid(seniorUuid).orElse(null);
        if (senior == null) {
            return BasicServiceResult.of(ServiceResult.GenericStatus.NOT_FOUND, null, null);
        }
        return BasicServiceResult.of(
                ServiceResult.GenericStatus.SUCCESS, senior.getMonitored(), null);
    }

    public BasicServiceResult<Void> setMonitoringEnabled(String seniorUuid, boolean monitored) {
        Senior senior = seniorRepository.findById(seniorUuid).orElse(null);
        if (senior == null) {
            return BasicServiceResult.of(ServiceResult.GenericStatus.NOT_FOUND, null, null);
        }
        senior.setMonitored(monitored);
        seniorRepository.save(senior);
        return BasicServiceResult.of(ServiceResult.GenericStatus.SUCCESS, null, null);
    }

    public BasicServiceResult<CareResponseDTO.StatBrief> addSensorStat(
            String seniorUuid, PostSensorStat dto) {
        try {
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));
            SeniorSensorStat stat = careMapper.toEntity(dto, senior);
            return BasicServiceResult.of(
                    ServiceResult.GenericStatus.SUCCESS,
                    careMapper.toStatBrief(seniorSensorStatRepository.save(stat)),
                    null);
        } catch (Exception e) {
            return BasicServiceResult.of(ServiceResult.GenericStatus.ERROR, null, e);
        }
    }

    public CareQueryResult<CareResponseDTO.AlertDetail> getAlertDetail(String eventUuid) {
        try {
            CareAlert alert = careAlertRepository.findByUuid(eventUuid).orElse(null);
            if (alert == null) {
                return CareQueryResult.of(CareQueryResult.Status.SENIOR_NOT_FOUND, null, null);
            }

            return CareQueryResult.of(
                    CareQueryResult.Status.SUCCESS, careMapper.toAlertDetail(alert), null);
        } catch (Exception e) {
            return CareQueryResult.of(CareQueryResult.Status.ERROR, null, e);
        }
    }

    public BasicServiceResult<CareResponseDTO.AlertBrief> addCareAlert(
            String seniorUuid, PostCareAlert dto) {
        try {
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));

            CareAlert alert = careAlertRepository.save(careMapper.toEntity(dto, senior));

            Map<String, String> eventData =
                    Map.of("eventUuid", alert.getUuid(), "seniorUuid", senior.getUuid());
            if (alert.getEventType() == SeniorEvent.CAMERA_BATTERY_LOW
                    || alert.getEventType() == SeniorEvent.WEARABLE_BATTERY_LOW) {
                pushService.pushBatch(
                        Role.SENIOR,
                        senior.getUuid(),
                        PushChannel.ALERT,
                        alert.getEventType().getTitle(),
                        alert.getEventType().getMessage(),
                        Optional.of(eventData));
            } else {
                careRelationRepository.findBySeniorOrderByMember_NameAsc(senior).stream()
                        .map(Care::getMember)
                        .forEach(
                                member ->
                                        pushService.pushBatch(
                                                Role.MEMBER,
                                                member.getUuid(),
                                                PushChannel.ALERT,
                                                String.format(
                                                        "[%s] %s(%s, %d세)",
                                                        alert.getEventType().getTitle(),
                                                        senior.getName(),
                                                        senior.getGender().getText(),
                                                        senior.getAge()),
                                                alert.getEventType().getMessage(),
                                                Optional.of(eventData)));
            }
            return BasicServiceResult.of(
                    ServiceResult.GenericStatus.SUCCESS, careMapper.toAlertBrief(alert), null);
        } catch (Exception e) {
            return BasicServiceResult.of(ServiceResult.GenericStatus.ERROR, null, e);
        }
    }

    // TODO: use pagination for alerts and stats if needed (by client request)
    public CareQueryResult<CareResponseDTO.SeniorDetail> getSeniorDetail(String seniorUuid) {
        try {
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));
            SeniorResponseDTO.Profile profile = seniorMapper.toProfileDTO(senior);
            List<CareResponseDTO.AlertBrief> alerts = getRecentAlertBriefs(seniorUuid);
            List<CareResponseDTO.StatBrief> stats = getRecentStatBriefs(seniorUuid);
            return CareQueryResult.of(
                    CareQueryResult.Status.SUCCESS,
                    new CareResponseDTO.SeniorDetail(profile, senior.getMonitored(), alerts, stats),
                    null);
        } catch (IllegalArgumentException e) {
            return CareQueryResult.of(CareQueryResult.Status.SENIOR_NOT_FOUND, null, e);
        } catch (Exception e) {
            return CareQueryResult.of(CareQueryResult.Status.ERROR, null, e);
        }
    }

    private List<CareResponseDTO.AlertBrief> getRecentAlertBriefs(String seniorUuid) {
        return careAlertRepository
                .findTop5BySenior_UuidAndIsDismissedIsFalseOrderByTimestampDesc(seniorUuid)
                .stream()
                .map(careMapper::toAlertBrief)
                .toList();
    }

    private List<CareResponseDTO.StatBrief> getRecentStatBriefs(String seniorUuid) {
        return seniorSensorStatRepository
                .findTop20BySenior_UuidOrderByTimestampDesc(seniorUuid)
                .stream()
                .map(careMapper::toStatBrief)
                .toList();
    }
}
