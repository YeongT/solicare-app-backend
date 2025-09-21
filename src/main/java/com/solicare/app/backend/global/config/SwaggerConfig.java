package com.solicare.app.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SwaggerConfig {
    @Value("${springdoc.server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI solicareAPI() {
        Info info =
                new Info()
                        .title("Solicare API")
                        .description("Solicare 서버의 API 명세서입니다.")
                        .version("1.0.0");

        String jwtSchemeName = "BearerAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components =
                new Components()
                        .addSecuritySchemes(
                                jwtSchemeName,
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description(
                                                "Enter your JWT token (without the Bearer prefix)"));

        return new OpenAPI()
                .openapi("3.0.1")
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public OpenApiCustomizer globalResponseOpenApiCustomizer() {
        return openApi -> {
            openApi.getServers().clear();
            openApi.addServersItem(new Server().url(serverUrl));
            openApi.getPaths()
                    .values()
                    .forEach(
                            pathItem ->
                                    pathItem.readOperations()
                                            .forEach(
                                                    operation -> {
                                                        ApiResponses responses =
                                                                operation.getResponses();
                                                        responses.addApiResponse(
                                                                "400",
                                                                new ApiResponse()
                                                                        .description("잘못된 요청"));
                                                        responses.addApiResponse(
                                                                "401",
                                                                new ApiResponse()
                                                                        .description("자격 증명 실패"));
                                                        responses.addApiResponse(
                                                                "403",
                                                                new ApiResponse()
                                                                        .description("권한 없음"));
                                                        responses.addApiResponse(
                                                                "404",
                                                                new ApiResponse()
                                                                        .description(
                                                                                "리소스를 찾을 수 없음"));
                                                        responses.addApiResponse(
                                                                "405",
                                                                new ApiResponse()
                                                                        .description(
                                                                                "허용되지 않은 메서드"));
                                                        responses.addApiResponse(
                                                                "409",
                                                                new ApiResponse()
                                                                        .description("중복/충돌"));
                                                        responses.addApiResponse(
                                                                "500",
                                                                new ApiResponse()
                                                                        .description("서버 내부 오류"));
                                                        responses.addApiResponse(
                                                                "422",
                                                                new ApiResponse()
                                                                        .description("유효성 검사 실패"));
                                                        responses.addApiResponse(
                                                                "429",
                                                                new ApiResponse()
                                                                        .description(
                                                                                "요청 제한(Too Many Requests)"));
                                                        responses.addApiResponse(
                                                                "502",
                                                                new ApiResponse()
                                                                        .description("게이트웨이 오류"));
                                                        responses.addApiResponse(
                                                                "503",
                                                                new ApiResponse()
                                                                        .description("서비스 일시 중단"));
                                                        responses.addApiResponse(
                                                                "504",
                                                                new ApiResponse()
                                                                        .description("게이트웨이 타임아웃"));
                                                    }));
        };
    }
}
