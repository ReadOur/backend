package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "user_interested_library")
@IdClass(UserInterestedLibraryId.class)
public class UserInterestedLibrary {
    @Id private Long userId;
    @Id private String libraryCode;
    private LocalDateTime createdAt;
}
