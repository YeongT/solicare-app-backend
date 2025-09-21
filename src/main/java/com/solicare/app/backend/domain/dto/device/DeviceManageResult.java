package com.solicare.app.backend.domain.dto.device;

import com.solicare.app.backend.application.dto.res.DeviceResponseDTO;
import com.solicare.app.backend.domain.dto.ServiceResult;
import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class DeviceManageResult implements ServiceResult {
    private Status status;
    private DeviceResponseDTO.Info response;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return status == Status.CREATED
                || status == Status.UPDATED
                || status == Status.DELETED
                || status == Status.LINKED
                || status == Status.UNLINKED;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        CREATED(ApiStatus._CREATED, "DEVICE201", "디바이스가 성공적으로 생성되었습니다."),
        UPDATED(ApiStatus._OK, "DEVICE200", "디바이스가 성공적으로 업데이트되었습니다."),
        DELETED(ApiStatus._OK, "DEVICE200", "디바이스가 성공적으로 삭제되었습니다."),
        ALREADY_EXISTS(ApiStatus._CONFLICT, "DEVICE409", "이미 존재하는 디바이스입니다."),
        LINKED(ApiStatus._OK, "DEVICE200", "디바이스가 성공적으로 연결되었습니다."),
        UNLINKED(ApiStatus._OK, "DEVICE200", "디바이스가 성공적으로 연결 해제되었습니다."),
        ALREADY_LINKED(ApiStatus._BAD_REQUEST, "DEVICE409", "이미 연결된 디바이스입니다."),
        DEVICE_NOT_FOUND(ApiStatus._NOT_FOUND, "DEVICE404", "디바이스를 찾을 수 없습니다."),
        MEMBER_NOT_FOUND(ApiStatus._NOT_FOUND, "MEMBER404", "멤버를 찾을 수 없습니다."),
        SENIOR_NOT_FOUND(ApiStatus._NOT_FOUND, "SENIOR404", "시니어를 찾을 수 없습니다."),
        ERROR(ApiStatus._INTERNAL_SERVER_ERROR, "DEVICE500", "디바이스 관리 중 오류가 발생했습니다.");

        private final ApiStatus apiStatus;
        private final String code;
        private final String message;
    }
}
