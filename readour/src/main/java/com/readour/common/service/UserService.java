package com.readour.common.service;

import com.readour.common.dto.FindIdRequestDto;
import com.readour.common.dto.PasswordChangeRequestDto;
import com.readour.common.dto.PasswordResetRequestDto;
import com.readour.common.dto.SignupRequestDto;
import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.UserStatus;
import com.readour.common.exception.CustomException;
import com.readour.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    private static final String TEMP_PASSWORD_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    // 회원가입
    public void signup(SignupRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    // 로그인 (JWT 발급 전 단계)
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 사용자입니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new CustomException(ErrorCode.USER_INACTIVE, "활성화되지 않은 계정입니다.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public String findUserEmail(FindIdRequestDto dto) {
        User user = userRepository.findByNicknameAndBirthDate(dto.getNickname(), dto.getBirthDate())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."));
        return user.getEmail();
    }

    public void resetPassword(PasswordResetRequestDto dto) {
        User user = userRepository.findByEmailAndNicknameAndBirthDate(dto.getEmail(), dto.getNickname(), dto.getBirthDate())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "입력하신 정보와 일치하는 계정이 없습니다."));

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        mailService.sendPasswordResetMail(user.getEmail(), temporaryPassword);
    }

    public void changePassword(Long userId, PasswordChangeRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(TEMP_PASSWORD_CHAR_POOL.length());
            builder.append(TEMP_PASSWORD_CHAR_POOL.charAt(index));
        }
        return builder.toString();
    }
}
