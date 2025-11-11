package com.readour.common.service;

import com.readour.common.dto.LoginRequestDto;
import com.readour.common.dto.TokenResponseDto;
import com.readour.common.entity.User;
import com.readour.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponseDto login(LoginRequestDto requestDto) {
        User user = userService.login(requestDto.getEmail(), requestDto.getPassword());
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        LocalDateTime accessTokenExpiresAt = jwtTokenProvider.getExpiration(accessToken);
        return TokenResponseDto.of(accessToken, accessTokenExpiresAt);
    }

    public void logout(Long userId) {
        // 현재는 Access Token만 사용하므로 서버에서 별도의 처리를 하지 않습니다.
    }
}
