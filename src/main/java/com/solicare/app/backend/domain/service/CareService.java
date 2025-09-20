package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.request.CareRequestDTO.PostCareAlert;
import com.solicare.app.backend.application.dto.request.CareRequestDTO.PostSensorStat;
import com.solicare.app.backend.application.dto.request.MemberRequestDTO;
import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.application.dto.res.SeniorResponseDTO;
import com.solicare.app.backend.application.mapper.CareMapper;
import com.solicare.app.backend.application.mapper.SeniorMapper;
import com.solicare.app.backend.domain.dto.BasicServiceResult;
import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.domain.dto.care.CareLinkResult;
import com.solicare.app.backend.domain.dto.care.CareQueryResult;
import com.solicare.app.backend.domain.entity.Care;
import com.solicare.app.backend.domain.entity.Member;
import com.solicare.app.backend.domain.entity.Senior;
import com.solicare.app.backend.domain.entity.SeniorSensorStat;
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
                    careRelationRepository.findByMemberOrderByMember_NameAsc(member).stream()
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
                    careRelationRepository.findBySeniorOrderBySenior_NameAsc(senior).stream()
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
            return CareLinkResult.of(
                    CareLinkResult.Status.SUCCESS, careMapper.toMemberBriefDTO(member), null);
        } catch (Exception e) {
            return CareLinkResult.of(CareLinkResult.Status.ERROR, null, e);
        }
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

    public BasicServiceResult<CareResponseDTO.AlertBrief> addCareAlert(
            String seniorUuid, PostCareAlert dto) {
        try {
            Senior senior =
                    seniorRepository
                            .findByUuid(seniorUuid)
                            .orElseThrow(() -> new IllegalArgumentException("SENIOR_NOT_FOUND"));
            return BasicServiceResult.of(
                    ServiceResult.GenericStatus.SUCCESS,
                    careMapper.toAlertBrief(
                            careAlertRepository.save(careMapper.toEntity(dto, senior))),
                    null);
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
