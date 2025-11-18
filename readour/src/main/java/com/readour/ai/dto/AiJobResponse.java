package com.readour.ai.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.chat.entity.AiJob;
import com.readour.common.exception.CustomException;
import com.readour.common.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "AI 작업 응답")
public class AiJobResponse {

    private final Long jobId;
    private final Long roomId;
    private final Long requesterId;
    private final String action;
    private final String taskType;
    private final String status;
    private final JsonNode payload;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    private final Integer latencyMs;
    private final Long costTokens;
    private final String error;

    public static AiJobResponse from(AiJob job, ObjectMapper objectMapper) {
        return AiJobResponse.builder()
                .jobId(job.getId())
                .roomId(job.getRoomId())
                .requesterId(job.getRequesterId())
                .action(job.getAction())
                .taskType(job.getTaskType())
                .status(job.getStatus())
                .payload(parse(job.getPayload(), objectMapper))
                .startedAt(job.getStartedAt())
                .endedAt(job.getEndedAt())
                .latencyMs(job.getLatencyMs())
                .costTokens(job.getCostTokens())
                .error(job.getError())
                .build();
    }

    private static JsonNode parse(String payload, ObjectMapper objectMapper) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 결과를 파싱할 수 없습니다.");
        }
    }
}

