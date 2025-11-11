package com.readour.common.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.DuplicateCheckResponseDto;
import com.readour.common.dto.FindIdRequestDto;
import com.readour.common.dto.FindIdResponseDto;
import com.readour.common.dto.PasswordChangeRequestDto;
import com.readour.common.dto.PasswordResetRequestDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import com.readour.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "회원 계정 관련 API")
public class UserController {

    private final UserService userService;

    @PostMapping("/find-id")
    @Operation(summary = "아이디(이메일) 찾기")
    public ResponseEntity<ApiResponseDto<FindIdResponseDto>> findUserId(@Valid @RequestBody FindIdRequestDto requestDto) {
        String email = userService.findUserEmail(requestDto);
        FindIdResponseDto body = new FindIdResponseDto(email);
        ApiResponseDto<FindIdResponseDto> response = ApiResponseDto.<FindIdResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(body)
                .message("회원 이메일을 조회했습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 찾기 - 임시 비밀번호 발급")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        userService.resetPassword(requestDto);
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("임시 비밀번호를 이메일로 발송했습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/change-password")
    @Operation(summary = "로그인 사용자의 비밀번호 변경")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                               @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보가 존재하지 않습니다.");
        }
        userService.changePassword(userPrincipal.getId(), requestDto);
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("비밀번호가 변경되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 검사")
    public ResponseEntity<ApiResponseDto<DuplicateCheckResponseDto>> checkEmail(@RequestParam @Email String email) {
        boolean available = userService.isEmailAvailable(email);
        ApiResponseDto<DuplicateCheckResponseDto> response = ApiResponseDto.<DuplicateCheckResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(new DuplicateCheckResponseDto(available))
                .message(available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 검사")
    public ResponseEntity<ApiResponseDto<DuplicateCheckResponseDto>> checkNickname(@RequestParam @NotBlank String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        ApiResponseDto<DuplicateCheckResponseDto> response = ApiResponseDto.<DuplicateCheckResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(new DuplicateCheckResponseDto(available))
                .message(available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.")
                .build();
        return ResponseEntity.ok(response);
    }
}
