package com.readour.common.dto;

import com.readour.common.entity.User;
import com.readour.common.enums.Gender;
import com.readour.community.dto.UserLibraryResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "사용자 설정 조회 응답 DTO")
public class UserSettingsResponseDto {

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "생년월일")
    private LocalDate birthDate;

    @Schema(description = "성별")
    private Gender gender;

    @Schema(description = "선호 도서관 목록")
    private List<UserLibraryResponseDto> preferredLibraries;

    public static UserSettingsResponseDto from(User user, List<UserLibraryResponseDto> libraries) {
        return UserSettingsResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .preferredLibraries(libraries)
                .build();
    }
}