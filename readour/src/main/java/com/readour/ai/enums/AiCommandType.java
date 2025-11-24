package com.readour.ai.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.readour.chat.enums.ChatRoomScope;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum AiCommandType {

    PUBLIC_SUMMARY(
            "ROOM_RECENT_SUMMARY",
            "CHAT_SUMMARY",
            Set.of(ChatRoomScope.PUBLIC),
            30,
            true,
            Set.of("SUMMARY", "PUBLIC_SUMMARY", "RECENT_SUMMARY", "요약")
    ),
    GROUP_QUESTION_GENERATOR(
            "DISCUSSION_QUESTION_GENERATOR",
            "DISCUSSION_ASSIST",
            Set.of(ChatRoomScope.GROUP),
            40,
            true,
            Set.of("QUESTION_GENERATOR", "QUESTIONS", "질문", "QUESTION")
    ),
    GROUP_KEYPOINTS(
            "DISCUSSION_KEYPOINTS",
            "DISCUSSION_ASSIST",
            Set.of(ChatRoomScope.GROUP),
            40,
            true,
            Set.of("KEYPOINTS", "SUMMARY_DISCUSSION", "요점", "요점추출")
    ),
    GROUP_CLOSING(
            "DISCUSSION_CLOSING_STATEMENT",
            "DISCUSSION_ASSIST",
            Set.of(ChatRoomScope.GROUP),
            40,
            true,
            Set.of("CLOSING", "CLOSING_STATEMENT", "마감문", "마감문초안")
    ),
    SESSION_START(
            "SESSION_START",
            "SESSION_CONTROL",
            Set.of(ChatRoomScope.GROUP),
            0,
            false,
            Set.of("SESSION_START", "SESSION_BEGIN", "START_SESSION", "세션시작", "토론시작")
    ),
    SESSION_SUMMARY_SLICE(
            "SESSION_SUMMARY_SLICE",
            "SESSION_ASSIST",
            Set.of(ChatRoomScope.GROUP),
            80,
            true,
            Set.of("SESSION_SUMMARY", "SLICE_SUMMARY", "세션요약", "부분요약")
    ),
    SESSION_END(
            "SESSION_END",
            "SESSION_CONTROL",
            Set.of(ChatRoomScope.GROUP),
            0,
            false,
            Set.of("SESSION_END", "END_SESSION", "세션종료", "토론종료")
    ),
    SESSION_CLOSING(
            "SESSION_CLOSING",
            "SESSION_ASSIST",
            Set.of(ChatRoomScope.GROUP),
            0,
            false,
            Set.of("SESSION_CLOSING", "세션마감", "세션마감문", "FINAL_CLOSING")
    );

    private static final int MIN_LIMIT = 5;
    private static final int MAX_LIMIT = 400;

    private final String action;
    private final String taskType;
    private final Set<ChatRoomScope> allowedScopes;
    private final int defaultMessageLimit;
    private final boolean requiresTranscript;
    private final Set<String> aliases;

    AiCommandType(String action,
                  String taskType,
                  Set<ChatRoomScope> allowedScopes,
                  int defaultMessageLimit,
                  boolean requiresTranscript,
                  Set<String> aliases) {
        this.action = action;
        this.taskType = taskType;
        this.allowedScopes = Collections.unmodifiableSet(allowedScopes);
        this.defaultMessageLimit = defaultMessageLimit;
        this.requiresTranscript = requiresTranscript;
        this.aliases = Collections.unmodifiableSet(new HashSet<>(aliases));
    }

    public String getAction() {
        return action;
    }

    public String getTaskType() {
        return taskType;
    }

    public int resolveLimit(Integer requested) {
        if (requested == null) {
            return defaultMessageLimit;
        }
        int clamped = Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, requested));
        return clamped;
    }

    public void ensureScopeAllowed(ChatRoomScope scope) {
        if (!allowedScopes.contains(scope)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 채팅방에서는 이 명령을 사용할 수 없습니다.");
        }
    }

    public boolean requiresTranscript() {
        return requiresTranscript;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AiCommandType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "command가 비어 있습니다.");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(type -> type.aliases.stream()
                        .map(alias -> alias.toUpperCase(Locale.ROOT))
                        .anyMatch(alias -> alias.equals(normalized)))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "지원하지 않는 command입니다: " + raw));
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
