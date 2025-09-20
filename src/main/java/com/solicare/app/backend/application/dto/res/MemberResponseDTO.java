package com.solicare.app.backend.application.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberResponseDTO {
    @Schema(name = "MemberLoginResponse", description = "로그인 응답 DTO")
    public record Login(
            @Schema(description = "로그인된 사용자 정보") Profile profile,
            @Schema(description = "JWT 토큰") String token) {}

    @Schema(name = "MemberProfileResponse", description = "회원정보 응답 DTO")
    public record Profile(
            @Schema(description = "이름") String name,
            @Schema(description = "이메일") String email,
            @Schema(description = "휴대폰번호") String phoneNumber) {}
}
