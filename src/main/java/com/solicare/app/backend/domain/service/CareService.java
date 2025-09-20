package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.request.MemberRequestDTO;
import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.application.mapper.CareMapper;
import com.solicare.app.backend.domain.dto.BasicServiceResult;
import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.domain.dto.care.CareLinkResult;
import com.solicare.app.backend.domain.dto.care.CareQueryResult;
import com.solicare.app.backend.domain.entity.Care;
import com.solicare.app.backend.domain.entity.Member;
import com.solicare.app.backend.domain.entity.Senior;
import com.solicare.app.backend.domain.repository.CareAlertRepository;
import com.solicare.app.backend.domain.repository.CareRelationRepository;
import com.solicare.app.backend.domain.repository.MemberRepository;
import com.solicare.app.backend.domain.repository.SeniorRepository;

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
    private final CareRelationRepository careRelationRepository;
    private final CareAlertRepository careAlertRepository;
    private final CareMapper careMapper;

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

    public CareQueryResult<CareResponseDTO.SeniorBrief> querySeniorByMember(String memberUuid) {
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

    public CareQueryResult<CareResponseDTO.MemberBrief> queryMemberBySenior(String seniorUuid) {
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
                    CareLinkResult.Status.SUCCESS, careMapper.toSeniorBriefDTO(senior), null);
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
    // TODO: refactor duplicate code above, use private methods to reduce redundancy
}
