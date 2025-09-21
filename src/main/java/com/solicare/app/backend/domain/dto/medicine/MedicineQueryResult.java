package com.solicare.app.backend.domain.dto.medicine;

import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class MedicineQueryResult<DataType> implements ServiceResult {
    private Status status;
    private List<DataType> response;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    @Getter
    @AllArgsConstructor
    public enum Status {
        SUCCESS(ApiStatus._CREATED, "MEDICINE200", "조회가 성공적으로 처리되었습니다."),
        NOT_FOUND(null, "MEDICINE404", "해당 리소스을 찾을 수 없습니다."),
        ERROR(null, "MEDICINE500", "조회 처리 중 오류가 발생했습니다.");

        private final ApiStatus apiStatus;
        private final String code;
        private final String message;
    }
    // TODO: migrate to use BasicServiceResult<MedicineResponseDTO.Info>
}
