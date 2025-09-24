package com.solicare.app.backend.domain.dto.member;

import com.solicare.app.backend.application.dto.res.MemberResponseDTO;
import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class MemberJoinResult implements ServiceResult {
    private Status status;
    private MemberResponseDTO.Login response;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        SUCCESS(ApiStatus._OK, "MEMBER201", "멤버 가입이 성공적으로 처리되었습니다."),
        ALREADY_TAKEN_EMAIL(ApiStatus._CONFLICT, "MEMBER409", "사용중인 이메일주소 입니다."),
        ALREADY_TAKEN_PHONE(ApiStatus._CONFLICT, "MEMBER409", "이미 등록된 전화번호 입니다."),
        ERROR(ApiStatus._INTERNAL_SERVER_ERROR, "MEMBER500", "멤버 가입 처리 중 오류가 발생했습니다.");

        private final ApiStatus apiStatus;
        private final String code;
        private final String message;
    }
}
