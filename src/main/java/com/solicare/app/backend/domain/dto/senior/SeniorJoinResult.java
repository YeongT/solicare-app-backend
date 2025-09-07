// SeniorJoinOutput.java
package com.solicare.app.backend.domain.dto.senior;

import com.solicare.app.backend.application.dto.res.SeniorResponseDTO;
import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class SeniorJoinResult implements ServiceResult {
    private Status status;
    private SeniorResponseDTO.Login response;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        SUCCESS(ApiStatus._OK, "SENIOR200", "시니어 가입이 성공적으로 처리되었습니다."),
        ALREADY_TAKEN_USERID(ApiStatus._CONFLICT, "SENIOR409", "이미 존재하는 아이디 입니다."),
        ERROR(ApiStatus._INTERNAL_SERVER_ERROR, "SENIOR500", "시니어 가입 처리 중 오류가 발생했습니다.");

        private final ApiStatus apiStatus;
        private final String code;
        private final String message;
    }
}
