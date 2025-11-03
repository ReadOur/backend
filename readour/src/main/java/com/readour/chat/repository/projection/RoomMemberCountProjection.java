package com.readour.chat.repository.projection;

public interface RoomMemberCountProjection {
    Long getRoomId();
    long getMemberCount();
}
