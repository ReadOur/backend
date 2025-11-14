package com.readour.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String JWT_EXCEPTION_ATTR = "jwt_exception";
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        CustomException customException = (CustomException) request.getAttribute(JWT_EXCEPTION_ATTR);
        ErrorCode errorCode = customException != null ? customException.getErrorCode() : ErrorCode.UNAUTHORIZED;
        String message = customException != null ? customException.getMessage() : errorCode.getDefaultMessage();

        ErrorResponseDto body = new ErrorResponseDto(errorCode.getStatus(), message, LocalDateTime.now());

        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    //@AuthenticationPrincipal UserPrincipal user;
}
