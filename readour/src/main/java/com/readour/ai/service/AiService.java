package com.readour.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.readour.ai.dto.AiCommandRequest;
import com.readour.ai.dto.AiJobResponse;
import com.readour.ai.enums.AiCommandType;
import com.readour.chat.entity.AiJob;
import com.readour.chat.entity.ChatAiSession;
import com.readour.chat.entity.ChatAiSessionSummary;
import com.readour.chat.entity.ChatMessage;
import com.readour.chat.entity.ChatRoom;
import com.readour.chat.enums.ChatAiSessionStatus;
import com.readour.chat.enums.ChatRoomScope;
import com.readour.chat.repository.AiJobRepository;
import com.readour.chat.repository.ChatAiSessionRepository;
import com.readour.chat.repository.ChatAiSessionSummaryRepository;
import com.readour.chat.repository.ChatMessageRepository;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.chat.repository.ChatRoomRepository;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiService {

    private static final DateTimeFormatter MSG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_LINE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
    private static final int MIN_SESSION_SLICE_MESSAGES = 5;
    private static final int ADAPTIVE_MIN_CONTEXT = 50;
    private static final int ADAPTIVE_MAX_CONTEXT = 400;
    private static final int MAX_TRANSCRIPT_ATTEMPTS = 4;
    private static final int MAX_TRANSCRIPT_DURATION_MS = 10_000;
    private static final Pattern MEANINGLESS_TEXT_PATTERN = Pattern.compile(
            "^[\\s\\p{Punct}]*(?:[ㅋㅎㅠㅜ]+|ㅇ+)[\\s\\p{Punct}]*$",
            Pattern.CASE_INSENSITIVE);

    private final AiJobRepository aiJobRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatAiSessionRepository chatAiSessionRepository;
    private final ChatAiSessionSummaryRepository chatAiSessionSummaryRepository;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    public AiService(AiJobRepository aiJobRepository,
                     ChatRoomRepository chatRoomRepository,
                     ChatRoomMemberRepository chatRoomMemberRepository,
                     ChatMessageRepository chatMessageRepository,
                     ChatAiSessionRepository chatAiSessionRepository,
                     ChatAiSessionSummaryRepository chatAiSessionSummaryRepository,
                     ObjectMapper objectMapper,
                     ChatClient.Builder chatClientBuilder) {
        this.aiJobRepository = aiJobRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatAiSessionRepository = chatAiSessionRepository;
        this.chatAiSessionSummaryRepository = chatAiSessionSummaryRepository;
        this.objectMapper = objectMapper;
        this.chatClient = chatClientBuilder.build();
    }

    public AiJobResponse executeRoomCommand(Long roomId,
                                            Long requesterId,
                                            AiCommandRequest request) {
        if (roomId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "roomId는 필수입니다.");
        }
        if (requesterId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "요청자 정보가 필요합니다.");
        }

        AiCommandType commandType = request.getCommand();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, requesterId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));

        ChatRoomScope scope;
        try {
            scope = ChatRoomScope.from(room.getScope());
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "지원하지 않는 채팅방 유형입니다.");
        }
        commandType.ensureScopeAllowed(scope);

        return switch (commandType) {
            case SESSION_START -> startSession(roomId, requesterId, scope);
            case SESSION_SUMMARY_SLICE -> summarizeSessionSlice(room, scope, requesterId, request);
            case SESSION_END -> endSession(roomId, requesterId, scope);
            case SESSION_CLOSING -> generateSessionClosing(room, scope, requesterId, request);
            default -> runTranscriptCommand(room, scope, requesterId, request, commandType);
        };
    }

    public void runScheduledSessionSummaries() {
        List<ChatAiSession> activeSessions = chatAiSessionRepository.findAllByStatus(ChatAiSessionStatus.ACTIVE);
        if (activeSessions.isEmpty()) {
            return;
        }

        for (ChatAiSession session : activeSessions) {
            try {
                summarizeSessionSliceScheduled(session);
            } catch (CustomException ex) {
                if (ex.getErrorCode() != ErrorCode.BAD_REQUEST) {
                    log.warn("세션 자동 요약 실패 sessionId={}, message={}", session.getId(), ex.getMessage());
                }
            } catch (Exception ex) {
                log.error("세션 자동 요약 처리 중 오류 sessionId={}", session.getId(), ex);
            }
        }
    }

    private AiJobResponse runTranscriptCommand(ChatRoom room,
                                               ChatRoomScope scope,
                                               Long requesterId,
                                               AiCommandRequest request,
                                               AiCommandType commandType) {
        List<Integer> contextWindows = resolveContextWindows(commandType, request.getMessageLimit());
        AiJobResponse lastExistingResponse = null;
        Instant budgetStart = Instant.now();
        int attempt = 0;

        for (int limit : contextWindows) {
            attempt++;
            if (attempt > MAX_TRANSCRIPT_ATTEMPTS) {
                break;
            }
            if (Duration.between(budgetStart, Instant.now()).toMillis() > MAX_TRANSCRIPT_DURATION_MS) {
                break;
            }

            List<ChatMessage> contextMessages = loadRecentMessages(room.getId(), limit);
            List<ChatMessage> filteredMessages = filterMeaningfulMessages(contextMessages);
            if (filteredMessages.isEmpty()) {
                continue;
            }

            String transcript = buildTranscript(filteredMessages);
            PromptBundle promptBundle = buildPrompt(commandType, room, transcript, request);

            ObjectNode scopeParam = buildRoomScopeNode(room.getId(), scope, filteredMessages.size());
            ObjectNode options = buildStandardOptions(commandType, request, filteredMessages.size());
            String dedupeKey = buildDedupeKey(room.getId(), commandType, filteredMessages);

            AiJob existingJob = aiJobRepository.findTopByDedupeKey(dedupeKey).orElse(null);
            if (existingJob != null) {
                if (!"COMPLETED".equals(existingJob.getStatus())) {
                    return AiJobResponse.from(existingJob, objectMapper);
                }
                lastExistingResponse = AiJobResponse.from(existingJob, objectMapper);
                continue;
            }

            Instant startedInstant = Instant.now();
            LocalDateTime startedAt = LocalDateTime.now();
            AiJob job = buildJob(room.getId(), requesterId, commandType, "ROOM", scopeParam, options, promptBundle, startedAt, dedupeKey);
            if (!"RUNNING".equals(job.getStatus())) {
                return AiJobResponse.from(job, objectMapper);
            }

            try {
                JsonNode payload = callModelAsJson(promptBundle);
                completeJob(job, payload, startedInstant);
                return AiJobResponse.from(job, objectMapper);
            } catch (CustomException ex) {
                failJob(job, startedInstant, ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.error("AI 명령 처리 중 오류", ex);
                failJob(job, startedInstant, "LLM 처리 중 오류");
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 응답 생성 중 오류가 발생했습니다.");
            }
        }

        if (lastExistingResponse != null) {
            return lastExistingResponse;
        }
        return buildFallbackResponse(room, scope, requesterId, commandType);
    }

    private AiJobResponse startSession(Long roomId,
                                       Long requesterId,
                                       ChatRoomScope scope) {
        chatAiSessionRepository.findByRoomIdAndStatus(roomId, ChatAiSessionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.CONFLICT, "이미 진행 중인 토론 세션이 있습니다.");
                });

        ChatAiSession session = ChatAiSession.builder()
                .roomId(roomId)
                .startedBy(requesterId)
                .startedAt(LocalDateTime.now())
                .status(ChatAiSessionStatus.ACTIVE)
                .build();
        ChatAiSession savedSession = chatAiSessionRepository.save(session);

        ObjectNode scopeNode = objectMapper.createObjectNode();
        scopeNode.put("roomId", roomId);
        scopeNode.put("sessionId", savedSession.getId());
        scopeNode.put("roomScope", scope.name());

        ObjectNode options = objectMapper.createObjectNode();
        options.put("command", AiCommandType.SESSION_START.name());

        LocalDateTime now = LocalDateTime.now();
        AiJob job = buildJob(roomId, requesterId, AiCommandType.SESSION_START, "SESSION", scopeNode, options, null, now, null);
        job.setStatus("COMPLETED");
        job.setStartedAt(now);
        job.setEndedAt(now);
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("sessionId", savedSession.getId());
        payload.put("status", savedSession.getStatus().name());
        job.setPayload(writeJson(payload));
        aiJobRepository.save(job);

        return AiJobResponse.from(job, objectMapper);
    }

    private AiJobResponse summarizeSessionSlice(ChatRoom room,
                                                ChatRoomScope scope,
                                                Long requesterId,
                                                AiCommandRequest request) {
        ChatAiSession session = chatAiSessionRepository.findByRoomIdAndStatus(room.getId(), ChatAiSessionStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "진행 중인 토론 세션이 없습니다."));

        int limit = AiCommandType.SESSION_SUMMARY_SLICE.resolveLimit(request.getMessageLimit());
        List<ChatMessage> sliceMessages = loadSessionSliceMessages(room.getId(), session.getLastSummaryMsgId(), limit);
        List<ChatMessage> filteredMessages = filterMeaningfulMessages(sliceMessages);
        if (filteredMessages.size() < MIN_SESSION_SLICE_MESSAGES) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "요약할 신규 메시지가 충분하지 않습니다.");
        }

        String transcript = buildTranscript(filteredMessages);
        PromptBundle promptBundle = buildSessionSlicePrompt(room, transcript);

        Long startMsgId = filteredMessages.get(0).getId();
        Long endMsgId = filteredMessages.get(filteredMessages.size() - 1).getId();

        ObjectNode scopeNode = objectMapper.createObjectNode();
        scopeNode.put("roomId", room.getId());
        scopeNode.put("sessionId", session.getId());
        scopeNode.put("roomScope", scope.name());
        scopeNode.put("sliceStartMsgId", startMsgId);
        scopeNode.put("sliceEndMsgId", endMsgId);

        ObjectNode options = objectMapper.createObjectNode();
        options.put("command", AiCommandType.SESSION_SUMMARY_SLICE.name());
        options.put("contextMessages", filteredMessages.size());
        if (request.getNote() != null && !request.getNote().isBlank()) {
            options.put("note", request.getNote());
        }

        Instant startedInstant = Instant.now();
        LocalDateTime startedAt = LocalDateTime.now();
        AiJob job = buildJob(room.getId(), requesterId, AiCommandType.SESSION_SUMMARY_SLICE, "SESSION", scopeNode, options, promptBundle, startedAt, null);

        try {
            JsonNode payload = callModelAsJson(promptBundle);
            completeJob(job, payload, startedInstant);

            ChatAiSessionSummary summary = ChatAiSessionSummary.builder()
                    .sessionId(session.getId())
                    .startMsgId(startMsgId)
                    .endMsgId(endMsgId)
                    .summaryPayload(writeJson(payload))
                    .build();
            chatAiSessionSummaryRepository.save(summary);

            session.setLastSummaryMsgId(endMsgId);
            chatAiSessionRepository.save(session);

            return AiJobResponse.from(job, objectMapper);
        } catch (CustomException ex) {
            failJob(job, startedInstant, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("세션 부분 요약 생성 중 오류", ex);
            failJob(job, startedInstant, "세션 부분 요약 실패");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "세션 요약을 생성하지 못했습니다.");
        }
    }

    private void summarizeSessionSliceScheduled(ChatAiSession session) {
        if (session.getStatus() != ChatAiSessionStatus.ACTIVE) {
            return;
        }

        ChatRoom room = chatRoomRepository.findById(session.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
        ChatRoomScope scope;
        try {
            scope = ChatRoomScope.from(room.getScope());
        } catch (IllegalArgumentException ex) {
            log.warn("지원하지 않는 채팅방 스코프로 세션 요약을 건너뜀 roomId={}, scope={}", room.getId(), room.getScope());
            return;
        }

        int limit = AiCommandType.SESSION_SUMMARY_SLICE.resolveLimit(null);
        List<ChatMessage> sliceMessages = loadSessionSliceMessages(session.getRoomId(), session.getLastSummaryMsgId(), limit);
        List<ChatMessage> filteredMessages = filterMeaningfulMessages(sliceMessages);
        if (filteredMessages.size() < MIN_SESSION_SLICE_MESSAGES) {
            return;
        }

        String transcript = buildTranscript(filteredMessages);
        PromptBundle promptBundle = buildSessionSlicePrompt(room, transcript);

        Long startMsgId = filteredMessages.get(0).getId();
        Long endMsgId = filteredMessages.get(filteredMessages.size() - 1).getId();
        String dedupeKey = buildSessionSliceDedupeKey(session.getId(), startMsgId, endMsgId);
        if (aiJobRepository.findTopByDedupeKey(dedupeKey).isPresent()) {
            return;
        }

        ObjectNode scopeNode = objectMapper.createObjectNode();
        scopeNode.put("roomId", room.getId());
        scopeNode.put("sessionId", session.getId());
        scopeNode.put("roomScope", scope.name());
        scopeNode.put("sliceStartMsgId", startMsgId);
        scopeNode.put("sliceEndMsgId", endMsgId);

        ObjectNode options = objectMapper.createObjectNode();
        options.put("command", AiCommandType.SESSION_SUMMARY_SLICE.name());
        options.put("contextMessages", filteredMessages.size());
        options.put("trigger", "SCHEDULED");

        Instant startedInstant = Instant.now();
        LocalDateTime startedAt = LocalDateTime.now();
        AiJob job = buildJob(room.getId(), session.getStartedBy(), AiCommandType.SESSION_SUMMARY_SLICE, "SESSION", scopeNode, options, promptBundle, startedAt, dedupeKey);

        try {
            JsonNode payload = callModelAsJson(promptBundle);
            completeJob(job, payload, startedInstant);

            ChatAiSessionSummary summary = ChatAiSessionSummary.builder()
                    .sessionId(session.getId())
                    .startMsgId(startMsgId)
                    .endMsgId(endMsgId)
                    .summaryPayload(writeJson(payload))
                    .build();
            chatAiSessionSummaryRepository.save(summary);

            session.setLastSummaryMsgId(endMsgId);
            chatAiSessionRepository.save(session);
        } catch (CustomException ex) {
            failJob(job, startedInstant, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("세션 자동 요약 생성 중 오류 sessionId={}", session.getId(), ex);
            failJob(job, startedInstant, "세션 자동 요약 실패");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "세션 자동 요약에 실패했습니다.");
        }
    }

    private AiJobResponse endSession(Long roomId,
                                     Long requesterId,
                                     ChatRoomScope scope) {
        ChatAiSession session = chatAiSessionRepository.findByRoomIdAndStatus(roomId, ChatAiSessionStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "진행 중인 토론 세션이 없습니다."));

        session.setStatus(ChatAiSessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setEndedBy(requesterId);
        chatAiSessionRepository.save(session);

        ObjectNode scopeNode = objectMapper.createObjectNode();
        scopeNode.put("roomId", roomId);
        scopeNode.put("sessionId", session.getId());
        scopeNode.put("roomScope", scope.name());

        ObjectNode options = objectMapper.createObjectNode();
        options.put("command", AiCommandType.SESSION_END.name());

        LocalDateTime now = LocalDateTime.now();
        AiJob job = buildJob(roomId, requesterId, AiCommandType.SESSION_END, "SESSION", scopeNode, options, null, now, null);
        job.setStatus("COMPLETED");
        job.setStartedAt(now);
        job.setEndedAt(now);
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("sessionId", session.getId());
        payload.put("status", session.getStatus().name());
        job.setPayload(writeJson(payload));
        aiJobRepository.save(job);

        return AiJobResponse.from(job, objectMapper);
    }

    private AiJobResponse generateSessionClosing(ChatRoom room,
                                                 ChatRoomScope scope,
                                                 Long requesterId,
                                                 AiCommandRequest request) {
        ChatAiSession session = chatAiSessionRepository
                .findTopByRoomIdAndStatusOrderByIdDesc(room.getId(), ChatAiSessionStatus.COMPLETED)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "종료된 토론 세션이 없습니다."));

        List<ChatAiSessionSummary> summaries = chatAiSessionSummaryRepository
                .findAllBySessionIdOrderByIdAsc(session.getId());
        if (summaries.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "세션 요약 기록이 없어 마감문을 생성할 수 없습니다.");
        }

        String digest = buildSessionDigest(summaries);
        PromptBundle promptBundle = buildSessionClosingPrompt(room, session, digest, request.getNote());

        ObjectNode scopeNode = objectMapper.createObjectNode();
        scopeNode.put("roomId", room.getId());
        scopeNode.put("sessionId", session.getId());
        scopeNode.put("roomScope", scope.name());

        ObjectNode options = objectMapper.createObjectNode();
        options.put("command", AiCommandType.SESSION_CLOSING.name());
        options.put("digestSegments", summaries.size());

        Instant startedInstant = Instant.now();
        LocalDateTime startedAt = LocalDateTime.now();
        AiJob job = buildJob(room.getId(), requesterId, AiCommandType.SESSION_CLOSING, "SESSION", scopeNode, options, promptBundle, startedAt, null);

        try {
            JsonNode payload = callModelAsJson(promptBundle);
            String markdown = buildClosingMarkdown(payload, room, session);

            ObjectNode responsePayload = objectMapper.createObjectNode();
            responsePayload.put("closingMarkdown", markdown);
            responsePayload.set("plan", payload);

            completeJob(job, responsePayload, startedInstant);
            return AiJobResponse.from(job, objectMapper);
        } catch (CustomException ex) {
            failJob(job, startedInstant, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("세션 마감문 생성 실패", ex);
            failJob(job, startedInstant, "마감문 생성 실패");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "세션 마감문 생성 중 오류가 발생했습니다.");
        }
    }

    private List<ChatMessage> loadRecentMessages(Long roomId, int limit) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(0, limit, sort);
        Slice<ChatMessage> slice = chatMessageRepository.findByRoomIdAndDeletedAtIsNull(roomId, pageable);
        List<ChatMessage> messages = new ArrayList<>(slice.getContent());
        if (!messages.isEmpty()) {
            Collections.reverse(messages);
        }
        return messages;
    }

    private List<ChatMessage> loadSessionSliceMessages(Long roomId, Long lastSummaryMsgId, int limit) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, limit, sort);
        Slice<ChatMessage> slice = lastSummaryMsgId == null
                ? chatMessageRepository.findByRoomIdAndDeletedAtIsNull(roomId, pageable)
                : chatMessageRepository.findByRoomIdAndDeletedAtIsNullAndIdGreaterThan(roomId, lastSummaryMsgId, pageable);
        return new ArrayList<>(slice.getContent());
    }

    private String buildTranscript(List<ChatMessage> messages) {
        DateTimeFormatter formatter = MSG_TIME_FORMATTER.withLocale(Locale.KOREA);
        return messages.stream()
                .map(message -> {
                    String timestamp = formatter.format(message.getCreatedAt());
                    String bodyText = extractBodyText(message);
                    return "[" + timestamp + "] user#" + message.getSenderId() + ": " + bodyText;
                })
                .collect(Collectors.joining("\n"));
    }

    private List<ChatMessage> filterMeaningfulMessages(List<ChatMessage> messages) {
        return messages.stream()
                .filter(this::isMeaningfulMessage)
                .toList();
    }

    private boolean isMeaningfulMessage(ChatMessage message) {
        String text = extractBodyText(message);
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        // 필수 내용 없이 감탄사/이모티콘만 있는 경우 제외
        if (MEANINGLESS_TEXT_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        // 한 글자 이내이면서 기호성 문자가 주를 이루면 제외
        if (trimmed.length() <= 1 && !Character.isLetterOrDigit(trimmed.charAt(0))) {
            return false;
        }
        return true;
    }

    private PromptBundle buildPrompt(AiCommandType type,
                                     ChatRoom room,
                                     String transcript,
                                     AiCommandRequest request) {
        String note = request.getNote();
        String commonHeader = """
                채팅방 이름: %s
                채팅방 설명: %s
                """.formatted(
                room.getName() != null ? room.getName() : "제목 없음",
                room.getDescription() != null ? room.getDescription() : "설명 없음"
        );

        if (note != null && !note.isBlank()) {
            commonHeader += "추가 지시사항: " + note + "\n";
        }

        String userContent;
        String systemContent;

        switch (type) {
            case PUBLIC_SUMMARY -> {
                systemContent = """
                        너는 독서 커뮤니티 공개 채팅방의 대화를 안전하게 요약하는 전문가 어시스턴트다.
                        응답은 반드시 한국어 JSON 문자열이어야 하며, 민감한 표현을 제거한다.
                        정보가 부족하면 더 넓은 대화 로그가 필요하다고 명시하고, 충분하면 즉시 응답한다.
                        """;
                userContent = commonHeader + """
                        최근 메시지 타임라인:
                        %s

                        지시사항:
                        - highlights 배열에는 주요 대화 주제를 시간 순서대로 2~3줄 요약한다.
                        - keywords 배열에는 핵심 키워드를 3개 적는다.
                        - JSON 예시 {"highlights":["..."],"keywords":["..."]} 구조를 따른다.
                        """.formatted(transcript);
            }
            case GROUP_QUESTION_GENERATOR -> {
                systemContent = """
                        너는 독서 모임 토론을 이어갈 추가 질문을 제안하는 조력자다.
                        기존 논의와 자연스럽게 연결되도록 중복되지 않는 질문을 제시하고, 답변은 JSON 문자열로만 응답한다.
                        정보가 부족하면 더 넓은 대화 로그가 필요하다고 명시하고, 충분하면 즉시 응답한다.
                        """;
                userContent = commonHeader + """
                        최근 토론 대화:
                        %s

                        지시사항:
                        - 질문은 2~3개 작성하고 'questions' 배열에 넣는다.
                        - 각 질문은 참여자들이 생각을 확장할 수 있는 주제로 작성한다.
                        - JSON 예시 {"questions":["...","..."]} 형식을 따른다.
                        """.formatted(transcript);
            }
            case GROUP_KEYPOINTS -> {
                systemContent = """
                        너는 독서 모임 토론의 핵심을 정리하고 공감대와 이견을 구분해 주는 비서다.
                        응답은 JSON 포맷이어야 하며 모든 텍스트는 한국어여야 한다.
                        정보가 부족하면 더 넓은 대화 로그가 필요하다고 명시하고, 충분하면 즉시 응답한다.
                        """;
                userContent = commonHeader + """
                        토론 타임라인:
                        %s

                        지시사항:
                        - topicSummary 배열에 주제/키워드 기반 요약을 3줄 이내로 작성한다.
                        - alignment 배열에 공감된 부분을 3줄 이내로, disagreement 배열에 의견이 갈린 지점을 3줄 이내로 작성한다.
                        - JSON 예시 {"topicSummary":["..."],"alignment":["..."],"disagreement":["..."]}.
                        """.formatted(transcript);
            }
            case GROUP_CLOSING -> {
                systemContent = """
                        너는 독서 모임 토론을 마무리하는 사회자다.
                        토론 내용을 바탕으로 자연스럽고 따뜻한 어조의 마감문을 작성하고, JSON 포맷으로 요약 결과를 반환한다.
                        정보가 부족하면 더 넓은 대화 로그가 필요하다고 명시하고, 충분하면 즉시 응답한다.
                        """;
                userContent = commonHeader + """
                        토론 타임라인:
                        %s

                        지시사항:
                        - closingStatement 필드에 3~4문장 분량의 마감문을 작성한다.
                        - 다음 만남에 대한 기대나 다음 행동을 부드럽게 제안한다.
                        - JSON 예시 {"closingStatement":"..."}.
                        """.formatted(transcript);
            }
            default -> throw new CustomException(ErrorCode.BAD_REQUEST, "지원하지 않는 명령 유형입니다.");
        }

        return new PromptBundle(systemContent, userContent);
    }

    private PromptBundle buildSessionSlicePrompt(ChatRoom room, String transcript) {
        String systemContent = """
                너는 독서 모임 실시간 토론에서 나온 메시지 일부를 빠르게 요약하는 비서다.
                응답은 JSON 형식으로 반환하며 한국어만 사용한다.
                """;
        String userContent = """
                채팅방: %s
                설명: %s

                이번 세그먼트 대화 로그:
                %s

                지시사항:
                - overview 배열에는 2~3줄로 이번 대화의 핵심을 시간 순으로 정리한다.
                - agreements 배열에는 공감대가 형성된 지점을 정리한다.
                - disagreements 배열에는 의견이 갈린 포인트를 정리한다.
                - notableIdeas 배열에는 추후 참고할 만한 인사이트나 질문을 적는다.
                - JSON 예시 {"overview":["..."],"agreements":["..."],"disagreements":["..."],"notableIdeas":["..."]}.
                """.formatted(
                room.getName() != null ? room.getName() : "제목 없음",
                room.getDescription() != null ? room.getDescription() : "설명 없음",
                transcript
        );
        return new PromptBundle(systemContent, userContent);
    }

    private PromptBundle buildSessionClosingPrompt(ChatRoom room,
                                                   ChatAiSession session,
                                                   String digest,
                                                   String note) {
        StringBuilder user = new StringBuilder();
        user.append("독서모임 이름: ")
                .append(room.getName() != null ? room.getName() : "제목 없음")
                .append("\n")
                .append("세션 시작: ").append(formatDateTime(session.getStartedAt()))
                .append("\n")
                .append("세션 종료: ").append(formatDateTime(session.getEndedAt()))
                .append("\n\n")
                .append("세션 요약 조각:\n")
                .append(digest)
                .append("\n\n")
                .append("지시사항:\n")
                .append("- 아래 JSON 구조로만 답변한다.\n")
                .append("- storyFlow: 서술형 2~3문단 배열\n")
                .append("- commonThemes: 공감된 핵심 주제 배열\n")
                .append("- disagreements: [{\"title\":\"...\",\"viewA\":\"...\",\"viewB\":\"...\",\"summary\":\"...\"}]\n")
                .append("- extras: 추가로 나눈 이야기 배열\n")
                .append("- nextSteps: 다음 모임 준비 사항 배열\n");
        if (note != null && !note.isBlank()) {
            user.append("- 추가 지시: ").append(note).append("\n");
        }

        String systemContent = """
                너는 독서 모임 토론 기록을 바탕으로 구조화된 정보를 반환하는 정리 전문가다.
                민감한 표현을 제거하고, 모든 텍스트는 자연스러운 한국어로 작성한다.
                JSON 외의 문자는 반환하지 마라.
                """;
        return new PromptBundle(systemContent, user.toString());
    }

    private JsonNode callModelAsJson(PromptBundle promptBundle) {
        String raw = chatClient.prompt()
                .system(promptBundle.systemPrompt())
                .user(promptBundle.userPrompt())
                .call()
                .content();

        if (raw == null || raw.isBlank()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "LLM 응답이 비어 있습니다.");
        }

        String sanitized = sanitizeRawResponse(raw);
        try {
            return objectMapper.readTree(sanitized);
        } catch (Exception ex) {
            log.error("LLM 응답 파싱 실패. raw={}", raw);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 응답을 파싱할 수 없습니다.");
        }
    }

    private AiJob buildJob(Long roomId,
                           Long requesterId,
                           AiCommandType commandType,
                           String scopeType,
                           ObjectNode scopeParam,
                           ObjectNode options,
                           PromptBundle promptBundle,
                           LocalDateTime startedAt,
                           String dedupeKey) {
        AiJob.AiJobBuilder builder = AiJob.builder()
                .roomId(roomId)
                .requesterId(requesterId)
                .action(commandType.getAction())
                .taskType(commandType.getTaskType())
                .scopeType(scopeType)
                .scopeParam(writeJson(scopeParam))
                .options(writeJson(options))
                .status("RUNNING")
                .startedAt(startedAt);

        if (promptBundle != null) {
            builder.prompt(promptBundle.systemPrompt() + "\n\n" + promptBundle.userPrompt());
        }
        if (dedupeKey != null) {
            builder.dedupeKey(dedupeKey);
            AiJob existing = aiJobRepository.findTopByDedupeKey(dedupeKey).orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        try {
            return aiJobRepository.save(builder.build());
        } catch (DataAccessException ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 작업 기록을 저장하지 못했습니다.");
        }
    }

    private void completeJob(AiJob job,
                             JsonNode payload,
                             Instant startedInstant) {
        job.setPayload(writeJson(payload));
        job.setStatus("COMPLETED");
        job.setEndedAt(LocalDateTime.now());
        job.setLatencyMs((int) Duration.between(startedInstant, Instant.now()).toMillis());
        aiJobRepository.save(job);
    }

    private void failJob(AiJob job,
                         Instant startedInstant,
                         String message) {
        job.setStatus("FAILED");
        job.setError(message);
        job.setEndedAt(LocalDateTime.now());
        job.setLatencyMs((int) Duration.between(startedInstant, Instant.now()).toMillis());
        aiJobRepository.save(job);
    }

    private ObjectNode buildRoomScopeNode(Long roomId,
                                          ChatRoomScope scope,
                                          int contextCount) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("roomId", roomId);
        node.put("roomScope", scope.name());
        node.put("contextMessages", contextCount);
        return node;
    }

    private ObjectNode buildStandardOptions(AiCommandType commandType,
                                            AiCommandRequest request,
                                            int contextCount) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("command", commandType.name());
        node.put("contextMessages", contextCount);
        if (request.getMessageLimit() != null) {
            node.put("requestedMessageLimit", request.getMessageLimit());
        }
        if (request.getNote() != null && !request.getNote().isBlank()) {
            node.put("note", request.getNote());
        }
        return node;
    }

    private String writeJson(ObjectNode node) {
        if (node == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "JSON 직렬화에 실패했습니다.");
        }
    }

    private String writeJson(JsonNode node) {
        if (node == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "JSON 직렬화에 실패했습니다.");
        }
    }

    private String extractBodyText(ChatMessage message) {
        String body = message.getBody();
        if (body == null || body.isBlank()) {
            return "[" + message.getType() + "]";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("text")) {
                return node.get("text").asText();
            }
            if (node.hasNonNull("content")) {
                return node.get("content").asText();
            }
            if (node.isTextual()) {
                return node.asText();
            }
            if (node.isArray()) {
                ArrayNode array = (ArrayNode) node;
                if (!array.isEmpty()) {
                    return array.get(0).asText();
                }
            }
            return node.toString();
        } catch (Exception ex) {
            return body;
        }
    }

    private String sanitizeRawResponse(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstBreak = trimmed.indexOf('\n');
            if (firstBreak > 0) {
                trimmed = trimmed.substring(firstBreak + 1);
            }
            int closing = trimmed.lastIndexOf("```");
            if (closing > 0) {
                trimmed = trimmed.substring(0, closing);
            }
        }
        return trimmed.trim();
    }

    private String buildDedupeKey(Long roomId,
                                  AiCommandType commandType,
                                  List<ChatMessage> messages) {
        String raw = roomId + ":" + commandType.name() + ":" + messages.stream()
                .map(msg -> msg.getId() + "|" + (msg.getBody() == null ? "" : msg.getBody()))
                .collect(Collectors.joining("#"));
        return hashRaw(raw);
    }

    private String buildSessionSliceDedupeKey(Long sessionId, Long startMsgId, Long endMsgId) {
        String raw = "SESSION_SLICE:" + sessionId + ":" + startMsgId + ":" + endMsgId;
        return hashRaw(raw);
    }

    private AiJobResponse buildFallbackResponse(ChatRoom room,
                                                ChatRoomScope scope,
                                                Long requesterId,
                                                AiCommandType commandType) {
        ObjectNode scopeParam = buildRoomScopeNode(room.getId(), scope, 0);
        ObjectNode options = buildStandardOptions(commandType, AiCommandRequest.builder().build(), 0);
        options.put("fallback", true);

        LocalDateTime now = LocalDateTime.now();
        AiJob job = buildJob(room.getId(), requesterId, commandType, "ROOM", scopeParam, options, null, now, null);
        job.setStatus("COMPLETED");
        job.setStartedAt(now);
        job.setEndedAt(now);
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("fallback", true);
        payload.put("reason", "INSUFFICIENT_CONTEXT");
        payload.put("message", "대화창에서 충분한 정보를 찾지 못했습니다. 더 오래된 메시지나 추가 설명을 제공해 주세요.");
        job.setPayload(writeJson(payload));
        aiJobRepository.save(job);
        return AiJobResponse.from(job, objectMapper);
    }

    private List<Integer> resolveContextWindows(AiCommandType commandType, Integer requestedLimit) {
        if (!commandType.requiresTranscript()) {
            return List.of(commandType.resolveLimit(requestedLimit));
        }

        int start = Math.max(commandType.resolveLimit(requestedLimit), ADAPTIVE_MIN_CONTEXT);
        List<Integer> windows = new ArrayList<>();
        int current = start;
        while (true) {
            windows.add(current);
            if (current >= ADAPTIVE_MAX_CONTEXT) {
                break;
            }
            int next = Math.min(current * 2, ADAPTIVE_MAX_CONTEXT);
            if (windows.contains(next)) {
                break;
            }
            current = next;
        }
        return windows;
    }

    private String hashRaw(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private String buildSessionDigest(List<ChatAiSessionSummary> summaries) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < summaries.size(); i++) {
            ChatAiSessionSummary summary = summaries.get(i);
            JsonNode payload = parsePayload(summary.getSummaryPayload());
            sb.append("세그먼트 ").append(i + 1).append(" (")
                    .append(summary.getStartMsgId()).append("~").append(summary.getEndMsgId()).append(")\n");
            appendArraySection(sb, "overview", payload);
            appendArraySection(sb, "agreements", payload);
            appendArraySection(sb, "disagreements", payload);
            appendArraySection(sb, "notableIdeas", payload);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void appendArraySection(StringBuilder sb, String fieldName, JsonNode payload) {
        if (payload == null || !payload.has(fieldName)) {
            return;
        }
        JsonNode arrayNode = payload.get(fieldName);
        if (!arrayNode.isArray() || arrayNode.isEmpty()) {
            return;
        }
        sb.append("- ").append(fieldName).append(": ");
        List<String> texts = new ArrayList<>();
        arrayNode.forEach(node -> texts.add(node.asText()));
        sb.append(String.join("; ", texts)).append("\n");
    }

    private JsonNode parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            log.warn("세션 요약 payload 파싱 실패", ex);
            return null;
        }
    }

    private String buildClosingMarkdown(JsonNode plan,
                                        ChatRoom room,
                                        ChatAiSession session) {
        List<String> storyFlow = extractStringList(plan, "storyFlow");
        List<String> commonThemes = extractStringList(plan, "commonThemes");
        List<JsonNode> disagreements = extractNodeList(plan, "disagreements");
        List<String> extras = extractStringList(plan, "extras");
        List<String> nextSteps = extractStringList(plan, "nextSteps");

        String dateLine = DATE_LINE_FORMATTER.format(resolveSessionDate(session));
        String roomName = room.getName() != null ? room.getName() : "제목 없음";

        StringBuilder markdown = new StringBuilder();
        markdown.append("# 📚 독서모임 활동 기록\n\n")
                .append("**날짜**\n").append(dateLine).append("\n\n")
                .append("**독서모임명**\n").append(roomName).append("\n\n")
                .append("**도서명**\n\n")
                .append("**저자명**\n\n")
                .append("**참석자 명단**\n\n")
                .append("---\n\n")
                .append("## 📝 오늘의 이야기 흐름\n")
                .append(String.join("\n", storyFlow))
                .append("\n\n")
                .append("## 🔍 나눔 내용 요약\n")
                .append("### ■ 공통적으로 나온 이야기\n");
        for (String theme : commonThemes) {
            markdown.append("- ").append(theme).append("\n");
        }
        markdown.append("\n")
                .append("## 🔀 서로 다른 의견 (구체적 차이까지 포함)\n");
        int index = 1;
        for (JsonNode diff : disagreements) {
            String title = diff.hasNonNull("title") ? diff.get("title").asText() : "주제 " + index;
            String viewA = diff.hasNonNull("viewA") ? diff.get("viewA").asText() : "";
            String viewB = diff.hasNonNull("viewB") ? diff.get("viewB").asText() : "";
            String summary = diff.hasNonNull("summary") ? diff.get("summary").asText() : "";
            markdown.append("### ").append(index).append(") ").append(title).append("\n");
            if (!viewA.isBlank()) {
                markdown.append("- **관점 A:** ").append(viewA).append("\n");
            }
            if (!viewB.isBlank()) {
                markdown.append("- **관점 B:** ").append(viewB).append("\n");
            }
            if (!summary.isBlank()) {
                markdown.append("- ➡️ 정리: ").append(summary).append("\n");
            }
            markdown.append("\n");
            index++;
        }

        markdown.append("## 💬 추가로 나왔던 이야기\n");
        for (String extra : extras) {
            markdown.append("- ").append(extra).append("\n");
        }
        markdown.append("\n")
                .append("## 📌 다음 모임 준비\n");
        for (String next : nextSteps) {
            markdown.append("- ").append(next).append("\n");
        }
        markdown.append("\n");
        return markdown.toString();
    }

    private List<String> extractStringList(JsonNode node, String field) {
        if (node == null || !node.has(field) || !node.get(field).isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        node.get(field).forEach(item -> result.add(item.asText()));
        return result;
    }

    private List<JsonNode> extractNodeList(JsonNode node, String field) {
        if (node == null || !node.has(field) || !node.get(field).isArray()) {
            return List.of();
        }
        List<JsonNode> result = new ArrayList<>();
        node.get(field).forEach(result::add);
        return result;
    }

    private LocalDate resolveSessionDate(ChatAiSession session) {
        LocalDateTime target = session.getEndedAt() != null ? session.getEndedAt() : session.getStartedAt();
        return target != null ? target.toLocalDate() : LocalDate.now();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.toString();
    }

    private record PromptBundle(String systemPrompt, String userPrompt) {
    }
}
