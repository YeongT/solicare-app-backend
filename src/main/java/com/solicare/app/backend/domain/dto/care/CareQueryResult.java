package com.solicare.app.backend.domain.dto.care;

import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CareQueryResult<ResponseDataType> implements ServiceResult {
    private Status status;
    private ResponseDataType response;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        SUCCESS(ApiStatus._OK, "CARE200", "정보가 성공적으로 조회되었습니다."),
        MEMBER_NOT_FOUND(ApiStatus._NOT_FOUND, "CARE404", "멤버를 찾을 수 없습니다."),
        SENIOR_NOT_FOUND(ApiStatus._NOT_FOUND, "CARE404", "시니어를 찾을 수 없습니다."),
        ERROR(ApiStatus._INTERNAL_SERVER_ERROR, "CARE500", "정보 조회 중 오류가 발생했습니다.");

        private final ApiStatus apiStatus;
        private final String code;
        private final String message;
    }
}
