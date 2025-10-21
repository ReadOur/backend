package com.readour.community.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class UserInterestedLibraryId implements Serializable {
    private Long userId;
    private String libraryCode;
}
