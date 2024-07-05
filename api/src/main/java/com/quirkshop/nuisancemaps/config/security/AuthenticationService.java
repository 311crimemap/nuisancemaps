package com.quirkshop.nuisancemaps.config.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import jakarta.servlet.http.HttpServletRequest;

public class AuthenticationService {

    // NB using annotation based env variables requires
    // injection and creates a nightmare with SecurityConfig - AuthentcationFilter.
    private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
    private static final String authToken = System.getenv("ADMIN_API_KEY");

    public static Authentication getAuthentication(HttpServletRequest request) {

        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);

        if (apiKey == null || !apiKey.equals(authToken)) {
            throw new BadCredentialsException("Invalid API Key");
        }

        return new ApiKeyAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}
