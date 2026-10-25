package com.nabin.bankingwallet.security;


import com.nabin.bankingwallet.common.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

/**
 * Returns a JSON 401 when a protected endpoint is called without a valid token.
 */
// • RestAuthenticationEntryPoint runs when someone who is not logged in (no token, bad token, expired token) calls a protected URL. It answers 401 with our JSON.
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(),
                          ApiError.of(UUID
                                              .randomUUID()
                                              .toString(), "UNAUTHORIZED",
                                      "Authentication required",
                                      HttpServletResponse.SC_UNAUTHORIZED,
                                      "UNAUTHORIZED", request.getRequestURI(),
                                      request.getMethod()));
    }
}
