package com.solicare.app.backend.application.dto.request;

import com.solicare.app.backend.application.enums.PushChannel;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PushRequestDTO {
    @Schema(name = "PushRequest", description = "푸시 메시지 발송 요청 DTO")
    public record FcmSend(
            @Schema(
                            description = "토큰",
                            example = "token",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "토큰은 필수입니다.")
                    String token,
            @Schema(description = "푸시 메시지 내용", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "body는 필수입니다.")
                    MessageBody body) {}

    @Schema(name = "PushRequestMessageBody", description = "푸시 메시지의 body를 담는 DTO")
    public record MessageBody(
            @Schema(
                            description = "푸시 채널",
                            example = "INFO",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "채널은 필수입니다.")
                    PushChannel channel,
            @Schema(
                            description = "알림 제목",
                            example = "테스트 알림",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "제목은 필수입니다.")
                    String title,
            @Schema(
                            description = "알림 내용",
                            example = "테스트 메시지입니다.",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "내용은 필수입니다.")
                    String message,
            @Schema(
                            description = "데이터",
                            example = "{\"key\":\"value\"}",
                            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    Map<String, String> data) {}

    @Schema(name = "PushRequestTokenBody", description = "푸시 토큰의 body를 담는 DTO")
    public record TokenBody(
            @Schema(
                            description = "토큰",
                            example = "token",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "토큰은 필수입니다.")
                    String token) {}
}
