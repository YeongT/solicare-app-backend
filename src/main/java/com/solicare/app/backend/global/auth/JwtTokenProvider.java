package com.solicare.app.backend.global.auth;

import com.solicare.app.backend.domain.dto.auth.JwtValidateResult;
import com.solicare.app.backend.domain.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenProvider {
  private final SecretKey SIGNING_KEY;
  private final int expirationMinutes;

  public JwtTokenProvider(
      @Value("${jwt.secretKey}") String secretKey,
      @Value("${jwt.expiration}") int expirationMinutes) {
    // Base64 디코딩 후 HMAC 키 생성 (AuthFilter와 동일)
    this.SIGNING_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    this.expirationMinutes = expirationMinutes;
  }

  public String createToken(List<Role> roles, String uuid) {
    Date now = new Date();
    return Jwts.builder()
        .subject(uuid)
        .claim("role", roles.stream().map(Role::name).toList())
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMinutes * 60 * 1000L))
        .signWith(SIGNING_KEY)
        .compact();
  }

  public JwtValidateResult validateToken(String token) {
    try {
      Jws<Claims> jwsClaims = parseToken(token);

      if (jwsClaims.getPayload().getExpiration().before(new Date()))
        return JwtValidateResult.of(JwtValidateResult.Status.EXPIRED, null, null);
      return JwtValidateResult.of(JwtValidateResult.Status.VALID, jwsClaims, null);
    } catch (Exception e) {
      return JwtValidateResult.of(JwtValidateResult.Status.INVALID, null, e);
    }
  }

  public Jws<Claims> parseToken(String token) {
    return Jwts.parser().verifyWith(SIGNING_KEY).build().parseSignedClaims(token);
  }
}
