package com.solicare.app.backend.application.mapper;

import com.solicare.app.backend.application.dto.res.CareResponseDTO;
import com.solicare.app.backend.domain.entity.Member;
import com.solicare.app.backend.domain.entity.Senior;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CareMapper {
    private final SeniorMapper seniorMapper;

    public CareResponseDTO.MemberBrief toMemberBriefDTO(Member member) {
        return new CareResponseDTO.MemberBrief(
                member.getUuid(), member.getName(), member.getEmail(), member.getPhoneNumber());
    }

    public CareResponseDTO.SeniorBrief toSeniorBriefDTO(Senior senior) {
        return new CareResponseDTO.SeniorBrief(senior.getUuid(), senior.getName(), null);
    }

    public CareResponseDTO.SeniorBrief toSeniorBriefDTO(Senior senior, Long unreadAlertCount) {
        return new CareResponseDTO.SeniorBrief(
                senior.getUuid(), senior.getName(), unreadAlertCount);
    }

    public CareResponseDTO.SeniorDetail toSeniorDetailDTO(Senior senior) {
        return new CareResponseDTO.SeniorDetail(seniorMapper.toProfileDTO(senior));
    }
}
