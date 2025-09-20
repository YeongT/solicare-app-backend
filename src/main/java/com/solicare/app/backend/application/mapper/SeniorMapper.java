package com.solicare.app.backend.application.mapper;

import com.solicare.app.backend.application.dto.request.SeniorRequestDTO;
import com.solicare.app.backend.application.dto.res.SeniorResponseDTO;
import com.solicare.app.backend.domain.entity.Senior;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SeniorMapper {
    private final PasswordEncoder passwordEncoder;

    public Senior toEntity(SeniorRequestDTO.Join dto) {
        return Senior.builder()
                .userId(dto.userId())
                .password(passwordEncoder.encode(dto.password())) // 비밀번호 암호화
                .name(dto.name())
                .age(dto.age())
                .gender(dto.gender())
                .phoneNumber(dto.phoneNumber())
                .address(dto.address())
                .note(dto.note())
                .build();
    }

    public SeniorResponseDTO.Profile toProfileDTO(Senior senior) {
        return new SeniorResponseDTO.Profile(
                senior.getName(),
                senior.getAge(),
                senior.getGender(),
                senior.getPhoneNumber(),
                senior.getAddress(),
                senior.getNote());
    }
}
