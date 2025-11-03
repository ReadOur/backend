package com.readour.chat.enums;

import java.util.Locale;

public enum ChatRoomScope {
    PRIVATE,
    PUBLIC,
    GROUP;

    public static ChatRoomScope from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("scope must not be null");
        }
        try {
            return ChatRoomScope.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported chat room scope: " + value, ex);
        }
    }
}
