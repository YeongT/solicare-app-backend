package com.solicare.app.backend.domain.dto.medicine;

import com.solicare.app.backend.application.dto.res.MedicineResponseDTO;
import com.solicare.app.backend.domain.dto.ServiceResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class MedicineDetailQueryResult implements ServiceResult {
    private Status status;
    private List<MedicineResponseDTO.DetailedInfo> response;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    @Getter
    @AllArgsConstructor
    public enum Status {
        SUCCESS(null, "MEDICINE200", "약품 상세 조회가 성공적으로 처리되었습니다."),
        NOT_FOUND(null, "MEDICINE404", "해당 리소스를 찾을 수 없습니다."),
        ERROR(null, "MEDICINE500", "약품 상세 조회 처리 중 오류가 발생했습니다.");

        private final Object apiStatus;
        private final String code;
        private final String message;
    }
    // TODO: migrate to use BasicServiceResult<MedicineResponseDTO.DetailedInfo>
}
