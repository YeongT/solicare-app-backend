package com.solicare.app.backend.global.auth;

import com.solicare.app.backend.domain.dto.BasicServiceResult;
import com.solicare.app.backend.domain.service.CareService;

import lombok.experimental.UtilityClass;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class AuthUtil {
    public static boolean isDeniedToAccessMemberByMember(
            Authentication authentication, String memberUuid) {
        Set<String> authorities = buildAuthoritiesSet(authentication);
        boolean isAdmin = authorities.contains("ROLE_ADMIN");
        boolean isMember = authorities.contains("ROLE_MEMBER");
        return !(isAdmin || isMember && authentication.getName().equals(memberUuid));
    }

    private static Set<String> buildAuthoritiesSet(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public static boolean isDeniedToAccessSeniorBySenior(
            Authentication authentication, String seniorUuid) {
        Set<String> authorities = buildAuthoritiesSet(authentication);
        if (authorities.contains("ROLE_ADMIN")) return false;

        boolean isSenior = authorities.contains("ROLE_SENIOR");

        if (isSenior) return !authentication.getName().equals(seniorUuid);
        return true;
    }

    public static boolean isDeniedToAccessSeniorByMemberOrSenior(
            CareService careService, Authentication authentication, String seniorUuid) {
        Set<String> authorities = buildAuthoritiesSet(authentication);
        if (authorities.contains("ROLE_ADMIN")) return false;

        boolean isMember = authorities.contains("ROLE_MEMBER");
        boolean isSenior = authorities.contains("ROLE_SENIOR");

        if (isSenior) return !authentication.getName().equals(seniorUuid);
        if (isMember) {
            BasicServiceResult<Boolean> careCheckResult =
                    careService.hasMemberAccessToSenior(authentication.getName(), seniorUuid);
            if (careCheckResult.getException() != null) {
                throw (RuntimeException) careCheckResult.getException();
            }
            return !careCheckResult.getPayload();
        }
        return true;
    }
}
