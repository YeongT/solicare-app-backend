package com.solicare.app.backend.domain.dto;

import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.global.res.ApiResponse;
import com.solicare.app.backend.global.res.ApiStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor(staticName = "of")
public class BasicServiceResult<PayloadDataType> implements ServiceResult {
    private ApiStatus status;
    @Setter private String message;
    private PayloadDataType payload;
    private Exception exception;

    public static <T> BasicServiceResult<T> of(
            GenericStatus genericStatus, T payload, Exception exception) {
        return BasicServiceResult.of(
                genericStatus.getApiStatus(), genericStatus.getText(), payload, exception);
    }

    @Override
    public boolean isSuccess() {
        return status == ApiStatus._OK || status == ApiStatus._CREATED;
    }

    public ResponseEntity<ApiResponse<PayloadDataType>> getApiResponse(
            ApiResponseFactory apiResponseFactory) {
        return apiResponseFactory.onResult(
                getStatus(), getStatus().getCode(), getMessage(), getPayload(), getException());
    }

    public ResponseEntity<ApiResponse<Void>> getApiResponseWithoutPayload(
            ApiResponseFactory apiResponseFactory) {
        return apiResponseFactory.onResult(
                getStatus(), getStatus().getCode(), getMessage(), null, getException());
    }
}
