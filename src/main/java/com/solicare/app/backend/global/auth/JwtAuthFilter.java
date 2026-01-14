package com.solicare.app.backend.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solicare.app.backend.application.factory.ApiResponseFactory;
import com.solicare.app.backend.domain.dto.auth.JwtValidateResult;
import com.solicare.app.backend.domain.enums.Role;
import com.solicare.app.backend.domain.repository.MemberRepository;
import com.solicare.app.backend.domain.repository.SeniorRepository;
import com.solicare.app.backend.global.res.ApiStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;
  private final SeniorRepository seniorRepository;
  private final ObjectMapper objectMapper;
  private final ApiResponseFactory apiResponseFactory;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header == null) {
      chain.doFilter(request, response);
      return;
    }

    if (!header.startsWith("Bearer ")) {
      sendErrorResponse(response, "Authorization Header is not Bearer format");
      return;
    }

    String token = header.substring(7).trim();

    if (token.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }

    JwtValidateResult output = jwtTokenProvider.validateToken(token);

    if (output.getStatus() != JwtValidateResult.Status.VALID) {
      sendJwtValidateErrorResponse(response, output);
      return;
    }

    Jws<Claims> jwsClaims = output.getJwsClaims();
    Claims claims = jwsClaims.getPayload();
    String subject = claims.getSubject();
    log.info("👤 Subject: {}", subject);
    List<String> roles =
        Optional.ofNullable(claims.get("role"))
            .filter(List.class::isInstance)
            .map(list -> (List<?>) list)
            .map(list -> list.stream().map(Object::toString).toList())
            .orElseGet(List::of);

    log.info("🔑 Roles from token: {}", roles);

    if (roles.isEmpty()) {
      sendErrorResponse(response, "Role claim is missing in token.");
      return;
    }

    List<GrantedAuthority> authorities = buildAuthoritiesFromRoles(claims.getSubject(), roles);

    if (authorities.isEmpty()) {
      sendErrorResponse(response, "No valid authorities found for user.");
      return;
    }

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    log.info(
        "✅ Authentication successful: {} with authorities: {}",
        authentication.getPrincipal(),
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
    chain.doFilter(request, response);
  }

  private void sendJwtValidateErrorResponse(HttpServletResponse response, JwtValidateResult output)
      throws IOException {
    String reason;
    switch (output.getStatus()) {
      case JwtValidateResult.Status.INVALID -> {
        reason = "Invalid token";
        Exception e = output.getException();
        if (e != null) {
          reason += ": " + e.getMessage();
        }
      }
      case JwtValidateResult.Status.EXPIRED -> reason = "Expired token";
      default -> reason = "Unknown token error";
    }
    sendErrorResponse(response, reason);
  }

  /**
   * 💥 추가된 헬퍼 메서드: Spring Security Filter 단에서 ApiResponse 형식의 에러 응답을 생성합니다.
   *
   * @param response HttpServletResponse
   * @param message 응답 메시지에 포함될 동적 메시지
   */
  private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(ApiStatus._UNAUTHORIZED.getHttpStatus().value());
    response.setContentType("application/json;charset=UTF-8");
    String jsonResponse =
        objectMapper.writeValueAsString(
            apiResponseFactory.onFailure(ApiStatus._UNAUTHORIZED, message));
    response.getWriter().write(jsonResponse);
  }

  @NonNull
  private List<GrantedAuthority> buildAuthoritiesFromRoles(String subject, List<String> roleNames) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roleNames) {
      try {
        if (validateRoleWithSubject(Role.valueOf(role), subject)) {
          authorities.add(() -> "ROLE_" + role);
        }
      } catch (IllegalArgumentException e) {
        log.warn("Invalid role found in token: {}", role);
      }
    }
    return authorities;
  }

  private boolean validateRoleWithSubject(Role role, String subject) {
    return switch (role) {
      case MEMBER -> memberRepository.existsByUuid(subject);
      case SENIOR -> seniorRepository.existsByUuid(subject);
      case ADMIN -> true;
    };
  }
}
