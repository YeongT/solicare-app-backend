package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.request.MemberRequestDTO;
import com.solicare.app.backend.application.dto.res.MemberResponseDTO;
import com.solicare.app.backend.application.mapper.MemberMapper;
import com.solicare.app.backend.domain.dto.member.MemberJoinResult;
import com.solicare.app.backend.domain.dto.member.MemberLoginResult;
import com.solicare.app.backend.domain.dto.member.MemberProfileResult;
import com.solicare.app.backend.domain.entity.Member;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.repository.MemberRepository;
import com.solicare.app.backend.global.auth.JwtTokenProvider;

import jakarta.transaction.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberService {
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    // ================== 회원 가입 ==================
    /**
     * 회원 가입 및 JWT 토큰 발급
     *
     * @param dto 회원 가입 요청 DTO
     * @return 가입 결과 및 토큰 정보
     */
    public MemberJoinResult createAndIssueToken(MemberRequestDTO.Join dto) {
        if (memberRepository.existsByEmail(dto.email())) {
            return MemberJoinResult.of(MemberJoinResult.Status.ALREADY_USED_EMAIL, null, null);
        }
        if (memberRepository.existsByPhoneNumber(dto.phoneNumber())) {
            return MemberJoinResult.of(MemberJoinResult.Status.ALREADY_USED_PHONE, null, null);
        }
        Member newMember = memberMapper.toEntity(dto);
        memberRepository.save(newMember);
        String jwtToken = jwtTokenProvider.createToken(List.of(Role.MEMBER), newMember.getUuid());
        return MemberJoinResult.of(
                MemberJoinResult.Status.SUCCESS,
                new MemberResponseDTO.Login(dto.name(), jwtToken),
                null);
    }

    // ================== 로그인 (토큰 발급) ==================
    /**
     * 로그인 및 JWT 토큰 발급
     *
     * @param dto 로그인 요청 DTO
     * @return 로그인 결과 및 토큰 정보
     */
    public MemberLoginResult loginAndIssueToken(MemberRequestDTO.Login dto) {
        Member member = memberRepository.findByEmail(dto.email()).orElse(null);
        if (member == null) {
            return MemberLoginResult.of(MemberLoginResult.Status.USER_NOT_FOUND, null, null);
        }
        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            return MemberLoginResult.of(MemberLoginResult.Status.INVALID_PASSWORD, null, null);
        }
        String jwtToken = jwtTokenProvider.createToken(List.of(Role.MEMBER), member.getUuid());
        return MemberLoginResult.of(
                MemberLoginResult.Status.SUCCESS,
                new MemberResponseDTO.Login(member.getName(), jwtToken),
                null);
    }

    public MemberProfileResult getProfile(String uuid) {
        Member member = memberRepository.findByUuid(uuid).orElse(null);
        if (member == null) {
            return MemberProfileResult.of(MemberProfileResult.Status.NOT_FOUND, null, null);
        }
        MemberResponseDTO.Profile profile = memberMapper.toProfileDTO(member);
        return MemberProfileResult.of(MemberProfileResult.Status.SUCCESS, profile, null);
    }
}
