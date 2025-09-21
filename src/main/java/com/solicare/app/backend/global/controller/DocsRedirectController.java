package com.solicare.app.backend.global.controller;

import io.swagger.v3.oas.annotations.Hidden;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Hidden
@Controller
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DocsRedirectController {
    @GetMapping("/docs")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
}
