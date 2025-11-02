package com.readour.common.entity;

import com.readour.common.enums.Gender;
import com.readour.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

// users
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate birthDate;
    private String nickname;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}