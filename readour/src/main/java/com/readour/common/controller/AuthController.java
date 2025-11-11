package com.readour.common.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.LoginRequestDto;
import com.readour.common.dto.SignupRequestDto;
import com.readour.common.dto.TokenResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import com.readour.common.service.AuthService;
import com.readour.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "JWT 인증 API")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);


    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ResponseEntity<ApiResponseDto<Void>> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.CREATED.value())
                .message("회원가입이 완료되었습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 - JWT 발급")
    public ResponseEntity<ApiResponseDto<TokenResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info(">>> [DEBUG] email = " + requestDto.getEmail());
        log.info(">>> [DEBUG] password = " + requestDto.getPassword());
        TokenResponseDto tokenResponse = authService.login(requestDto);
        ApiResponseDto<TokenResponseDto> response = ApiResponseDto.<TokenResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(tokenResponse)
                .message("로그인에 성공했습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 - 액세스 토큰 무효화(클라이언트 처리)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseDto<Void>> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보가 존재하지 않습니다.");
        }
        authService.logout(userPrincipal.getId());
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("로그아웃이 완료되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }
}
