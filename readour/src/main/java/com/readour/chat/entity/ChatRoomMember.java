package com.readour.chat.entity;

import com.readour.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "chat_room_member")
@IdClass(ChatRoomMemberId.class)
public class ChatRoomMember {
    @Id private Long roomId;
    @Id private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;
    private LocalDateTime joinedAt;
    private LocalDateTime mutedUntil;
    private Long lastReadMsgId;
    private Boolean isActive;
    private LocalDateTime pinnedAt;
    private Integer pinOrder;
    private LocalDateTime kickedAt;
    private Long kickedBy;
    private String kickReason;
}
