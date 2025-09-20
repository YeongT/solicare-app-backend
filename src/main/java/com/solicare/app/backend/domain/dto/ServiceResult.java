package com.solicare.app.backend.domain.dto;

import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface ServiceResult {
    boolean isSuccess();

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum GenericStatus {
        SUCCESS(ApiStatus._OK, "요청하신 작업이 성공적으로 완료되었습니다."),
        NOT_FOUND(ApiStatus._NOT_FOUND, "요청하신 자원을 찾을 수 없습니다."),
        ERROR(ApiStatus._INTERNAL_SERVER_ERROR, "요청하신 작업을 처리하는 도중 오류가 발생했습니다.");
        private final ApiStatus apiStatus;
        private final String text;
    }
}
