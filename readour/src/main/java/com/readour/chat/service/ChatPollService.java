package com.readour.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.dto.event.ChatPollEvent;
import com.readour.chat.dto.request.ChatPollCreateRequest;
import com.readour.chat.dto.request.ChatPollVoteRequest;
import com.readour.chat.dto.response.ChatPollOptionResponse;
import com.readour.chat.dto.response.ChatPollResponse;
import com.readour.chat.dto.response.ChatPollResultResponse;
import com.readour.chat.dto.response.ChatScheduleCreatorResponse;
import com.readour.chat.entity.ChatMessage;
import com.readour.chat.entity.ChatPollVote;
import com.readour.chat.enums.ChatRoomScope;
import com.readour.chat.repository.ChatMessageRepository;
import com.readour.chat.repository.ChatPollVoteRepository;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.chat.repository.ChatRoomRepository;
import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Role;
import com.readour.common.exception.CustomException;
import com.readour.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatPollService {

    private static final String MESSAGE_TYPE_POLL = "POLL";
    private static final String POLL_TOPIC = "chat-polls";

    private final ChatMessageRepository chatMessageRepository;
    private final ChatPollVoteRepository chatPollVoteRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatPollResponse createPoll(Long roomId,
                                       Long actorId,
                                       ChatPollCreateRequest request) {
        ChatRoomScope scope = resolveScope(roomId);
        ensurePollCreationPermission(scope, roomId, actorId);
        validateOptions(request.getOptions());

        PollContent content = buildPollContent(request);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("question", content.question());
        if (content.description() != null && !content.description().isBlank()) {
            body.put("description", content.description());
        }
        body.put("multipleChoice", content.multipleChoice());
        if (content.closesAt() != null) {
            body.put("closesAt", content.closesAt().toString());
        }
        body.set("options", toOptionsArray(content.options()));

        MessageDto message = MessageDto.builder()
                .roomId(roomId)
                .senderId(actorId)
                .type(MESSAGE_TYPE_POLL)
                .body(body)
                .build();

        MessageDto saved = chatMessageService.send(message);

        ChatScheduleCreatorResponse creator = buildCreator(roomId, actorId);
        List<ChatPollOptionResponse> optionResponses = buildOptionResponses(content.options(), Map.of());

        ChatPollResponse response = ChatPollResponse.from(
                saved.getId(),
                roomId,
                content.question(),
                content.description(),
                content.multipleChoice(),
                content.closesAt(),
                saved.getCreatedAt(),
                saved.getCreatedAt(),
                optionResponses,
                creator
        );

        ChatPollResultResponse result = ChatPollResultResponse.builder()
                .pollId(saved.getId())
                .roomId(roomId)
                .totalVotes(0L)
                .options(optionResponses)
                .build();

        publishPollEvent("CREATED", actorId, response, result);

        return response;
    }

    @Transactional
    public ChatPollResultResponse vote(Long roomId,
                                       Long pollId,
                                       Long userId,
                                       ChatPollVoteRequest request) {
        ChatMessage pollMessage = loadPollMessage(roomId, pollId);
        ensureActiveMember(roomId, userId);

        PollContent content = parsePollContent(pollMessage);

        if (content.closesAt() != null && content.closesAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "투표가 종료되었습니다.");
        }

        List<String> selections = request.getSelections().stream()
                .filter(selection -> selection != null && !selection.isBlank())
                .distinct()
                .collect(Collectors.toList());
        validateVoteSelections(content, selections);

        ChatPollVote vote = ChatPollVote.builder()
                .pollMsgId(pollId)
                .userId(userId)
                .selectedOptions(writeJsonArray(selections))
                .votedAt(LocalDateTime.now())
                .build();

        chatPollVoteRepository.save(vote);

        return buildAndPublishResult("VOTE_UPDATED", userId, pollMessage, content);
    }

    @Transactional(readOnly = true)
    public ChatPollResultResponse getResult(Long roomId,
                                            Long pollId,
                                            Long requesterId) {
        ChatMessage pollMessage = loadPollMessage(roomId, pollId);
        ensureActiveMember(roomId, requesterId);

        PollContent content = parsePollContent(pollMessage);

        if (content.closesAt() != null && content.closesAt().isAfter(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "투표가 아직 종료되지 않았습니다.");
        }

        return buildResult(pollMessage, content);
    }

    private ChatPollResultResponse buildAndPublishResult(String action,
                                                         Long actorId,
                                                         ChatMessage pollMessage,
                                                         PollContent content) {
        Map<String, Long> counts = countAllVotes(pollMessage.getId());
        List<ChatPollOptionResponse> optionResponses = buildOptionResponses(content.options(), counts);
        long totalVotes = counts.values().stream().mapToLong(Long::longValue).sum();

        ChatScheduleCreatorResponse creator = buildCreator(pollMessage.getRoomId(), pollMessage.getSenderId());

        ChatPollResponse poll = ChatPollResponse.from(
                pollMessage.getId(),
                pollMessage.getRoomId(),
                content.question(),
                content.description(),
                content.multipleChoice(),
                content.closesAt(),
                pollMessage.getCreatedAt(),
                pollMessage.getCreatedAt(),
                optionResponses,
                creator
        );

        ChatPollResultResponse result = ChatPollResultResponse.builder()
                .pollId(pollMessage.getId())
                .roomId(pollMessage.getRoomId())
                .totalVotes(totalVotes)
                .options(optionResponses)
                .build();

        publishPollEvent(action, actorId, poll, result);

        return result;
    }

    private ChatPollResultResponse buildResult(ChatMessage pollMessage, PollContent content) {
        Map<String, Long> counts = countAllVotes(pollMessage.getId());
        List<ChatPollOptionResponse> optionResponses = buildOptionResponses(content.options(), counts);
        long totalVotes = counts.values().stream().mapToLong(Long::longValue).sum();

        return ChatPollResultResponse.builder()
                .pollId(pollMessage.getId())
                .roomId(pollMessage.getRoomId())
                .totalVotes(totalVotes)
                .options(optionResponses)
                .build();
    }

    private PollContent buildPollContent(ChatPollCreateRequest request) {
        List<PollOption> options = new ArrayList<>();
        int index = 1;
        for (String option : request.getOptions()) {
            options.add(new PollOption("opt_" + index++, option));
        }

        return new PollContent(
                request.getQuestion(),
                request.getDescription(),
                Boolean.TRUE.equals(request.getMultipleChoice()),
                request.getClosesAt(),
                List.copyOf(options)
        );
    }

    private ArrayNode toOptionsArray(List<PollOption> options) {
        ArrayNode array = objectMapper.createArrayNode();
        for (PollOption option : options) {
            ObjectNode node = array.addObject();
            node.put("id", option.id());
            node.put("text", option.text());
        }
        return array;
    }

    private ChatMessage loadPollMessage(Long roomId, Long pollId) {
        return chatMessageRepository.findById(pollId)
                .filter(message -> message.getDeletedAt() == null)
                .filter(message -> roomId.equals(message.getRoomId()))
                .filter(message -> MESSAGE_TYPE_POLL.equals(message.getType()))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "투표를 찾을 수 없습니다."));
    }

    private PollContent parsePollContent(ChatMessage message) {
        String body = message.getBody();
        if (body == null || body.isBlank()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "투표 정보가 손상되었습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            String question = getRequiredText(root, "question");
            String description = readNullableText(root, "description");
            boolean multipleChoice = root.path("multipleChoice").asBoolean(false);
            LocalDateTime closesAt = parseNullableDateTime(root.get("closesAt"));

            JsonNode optionsNode = root.get("options");
            if (optionsNode == null || !optionsNode.isArray() || !optionsNode.elements().hasNext()) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "투표 선택지 정보가 손상되었습니다.");
            }

            List<PollOption> options = new ArrayList<>();
            optionsNode.forEach(optionNode -> {
                String id = getRequiredText(optionNode, "id");
                String text = getRequiredText(optionNode, "text");
                options.add(new PollOption(id, text));
            });

            return new PollContent(question, description, multipleChoice, closesAt, List.copyOf(options));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "투표 정보를 읽을 수 없습니다.");
        }
    }

    private String getRequiredText(JsonNode node, String fieldName) {
        JsonNode target = node.get(fieldName);
        if (target == null || target.isNull() || target.asText().isBlank()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "필수 투표 정보가 누락되었습니다: " + fieldName);
        }
        return target.asText();
    }

    private String readNullableText(JsonNode node, String fieldName) {
        JsonNode target = node.get(fieldName);
        if (target == null || target.isNull()) {
            return null;
        }
        String value = target.asText();
        return value.isBlank() ? null : value;
    }

    private LocalDateTime parseNullableDateTime(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "투표 종료 시간이 올바르지 않습니다.");
        }
    }

    private List<ChatPollOptionResponse> buildOptionResponses(List<PollOption> options,
                                                              Map<String, Long> counts) {
        return options.stream()
                .map(option -> ChatPollOptionResponse.builder()
                        .id(option.id())
                        .text(option.text())
                        .voteCount(counts.getOrDefault(option.id(), 0L))
                        .build())
                .collect(Collectors.toList());
    }

    private void publishPollEvent(String action,
                                  Long actorId,
                                  ChatPollResponse poll,
                                  ChatPollResultResponse result) {
        ChatPollEvent event = ChatPollEvent.builder()
                .action(action)
                .roomId(poll.getRoomId())
                .pollId(poll.getId())
                .actorId(actorId)
                .poll(poll)
                .result(result)
                .build();

        kafkaTemplate.send(POLL_TOPIC, String.valueOf(poll.getRoomId()), eventToJson(event));
    }

    private String eventToJson(ChatPollEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "투표 이벤트 직렬화에 실패했습니다.");
        }
    }

    private ChatScheduleCreatorResponse buildCreator(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자를 찾을 수 없습니다."));

        Role role = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .map(com.readour.chat.entity.ChatRoomMember::getRole)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자의 채팅방 역할을 찾을 수 없습니다."));

        return ChatScheduleCreatorResponse.builder()
                .id(userId)
                .username(user.getNickname())
                .role(role)
                .build();
    }

    private void ensurePollCreationPermission(ChatRoomScope scope, Long roomId, Long actorId) {
        com.readour.chat.entity.ChatRoomMember member = ensureActiveMember(roomId, actorId);
        if (scope == ChatRoomScope.PRIVATE) {
            return;
        }
        if (!isManagerOrOwner(member)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "투표 생성은 방장 또는 매니저만 가능합니다.");
        }
    }

    private com.readour.chat.entity.ChatRoomMember ensureActiveMember(Long roomId, Long userId) {
        return chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));
    }

    private boolean isManagerOrOwner(com.readour.chat.entity.ChatRoomMember member) {
        Role role = member.getRole();
        return role == Role.MANAGER || role == Role.OWNER;
    }

    private ChatRoomScope resolveScope(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .map(room -> ChatRoomScope.from(room.getScope()))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }

    private void validateOptions(List<String> options) {
        if (options == null || options.size() < 2) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "투표 선택지는 최소 2개 이상이어야 합니다.");
        }
        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "빈 선택지는 허용되지 않습니다.");
            }
        }
    }

    private void validateVoteSelections(PollContent content, List<String> selections) {
        if (selections == null || selections.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "선택한 옵션이 없습니다.");
        }

        if (!content.multipleChoice() && selections.size() > 1) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "복수 선택이 불가능한 투표입니다.");
        }

        List<String> optionIds = content.options().stream()
                .map(PollOption::id)
                .collect(Collectors.toList());

        for (String selection : selections) {
            if (!optionIds.contains(selection)) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "존재하지 않는 투표 선택지가 포함되어 있습니다.");
            }
        }
    }

    private Map<String, Long> countAllVotes(Long pollId) {
        List<ChatPollVote> votes = chatPollVoteRepository.findAllByPollMsgId(pollId);
        Map<String, Long> counts = new HashMap<>();
        for (ChatPollVote vote : votes) {
            List<String> selections = readSelections(vote.getSelectedOptions());
            for (String selection : selections) {
                counts.put(selection, counts.getOrDefault(selection, 0L) + 1);
            }
        }
        return counts;
    }

    private List<String> readSelections(String selectionsJson) {
        if (selectionsJson == null || selectionsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(selectionsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "투표 선택지를 읽어오는데 실패했습니다.");
        }
    }

    private String writeJsonArray(List<String> selections) {
        try {
            return objectMapper.writeValueAsString(selections);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "투표 선택지를 저장하는데 실패했습니다.");
        }
    }

    private record PollOption(String id, String text) { }

    private record PollContent(String question,
                               String description,
                               boolean multipleChoice,
                               LocalDateTime closesAt,
                               List<PollOption> options) { }
}
