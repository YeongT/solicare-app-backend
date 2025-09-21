package com.solicare.app.backend.domain.dto.medicine;

import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class MedicineCreateResult<CreatedInfoDTODataType> implements ServiceResult {
    private Status status;
    private CreatedInfoDTODataType response;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        SUCCESS(ApiStatus._CREATED, "MEDICINE201", "생성 작업이 성공적으로 처리되었습니다."),
        DELETE_SUCCESS(ApiStatus._OK, "MEDICINE200", "삭제 작업이 성공적으로 처리되었습니다."),
        NOT_FOUND(ApiStatus._NOT_FOUND, "MEDICINE404", "해당 리소스를 찾을 수 없습니다."),
        ALREADY_EXIST(ApiStatus._CONFLICT, "MEDICINE409", "이미 존재하는 약품입니다."),
        ERROR(ApiStatus._INTERNAL_SERVER_ERROR, "MEDICINE500", "약품 등록 처리 중 오류가 발생했습니다.");

        private final ApiStatus apiStatus;
        private final String code;
        private final String message;
    }
    // TODO: migrate to use BasicServiceResult<MedicineResponseDTO.Info>
}
