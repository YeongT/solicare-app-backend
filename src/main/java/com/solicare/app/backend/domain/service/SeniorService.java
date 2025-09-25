package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.SeniorResponseDTO;
import com.solicare.app.backend.application.mapper.SeniorMapper;
import com.solicare.app.backend.domain.dto.senior.SeniorJoinResult;
import com.solicare.app.backend.domain.dto.senior.SeniorLoginResult;
import com.solicare.app.backend.domain.dto.senior.SeniorProfileResult;
import com.solicare.app.backend.domain.entity.Senior;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.repository.SeniorRepository;
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
public class SeniorService {
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SeniorRepository seniorRepository;
    private final SeniorMapper seniorMapper;

    /** Senior 회원 가입 및 JWT 토큰 발급 */
    public SeniorJoinResult createAndIssueToken(SeniorRequestDTO.Join dto) {
        if (seniorRepository.existsByUserId(dto.userId())) {
            return SeniorJoinResult.of(SeniorJoinResult.Status.ALREADY_TAKEN_USERID, null, null);
        }
        if (seniorRepository.existsByPhoneNumber(dto.phoneNumber())) {
            return SeniorJoinResult.of(SeniorJoinResult.Status.ALREADY_TAKEN_PHONE, null, null);
        }

        Senior newSenior = seniorMapper.toEntity(dto);
        seniorRepository.save(newSenior);

        String jwtToken = jwtTokenProvider.createToken(List.of(Role.SENIOR), newSenior.getUuid());
        return SeniorJoinResult.of(
                SeniorJoinResult.Status.SUCCESS,
                new SeniorResponseDTO.Login(seniorMapper.toProfileDTO(newSenior), jwtToken),
                null);
    }

    /** Senior 로그인 및 JWT 토큰 발급 */
    public SeniorLoginResult loginAndIssueToken(SeniorRequestDTO.Login dto) {
        Senior senior = seniorRepository.findByUserId(dto.userId()).orElse(null);
        if (senior == null) {
            return SeniorLoginResult.of(SeniorLoginResult.Status.SENIOR_NOT_FOUND, null, null);
        }

        if (!passwordEncoder.matches(dto.password(), senior.getPassword())) {
            return SeniorLoginResult.of(SeniorLoginResult.Status.INVALID_PASSWORD, null, null);
        }

        String jwtToken = jwtTokenProvider.createToken(List.of(Role.SENIOR), senior.getUuid());
        return SeniorLoginResult.of(
                SeniorLoginResult.Status.SUCCESS,
                new SeniorResponseDTO.Login(seniorMapper.toProfileDTO(senior), jwtToken),
                null);
    }

    public SeniorProfileResult getProfile(String uuid) {
        Senior senior = seniorRepository.findByUuid(uuid).orElse(null);
        if (senior == null) {
            return SeniorProfileResult.of(SeniorProfileResult.Status.NOT_FOUND, null, null);
        }
        SeniorResponseDTO.Profile profile = seniorMapper.toProfileDTO(senior);
        return SeniorProfileResult.of(SeniorProfileResult.Status.SUCCESS, profile, null);
    }
}
