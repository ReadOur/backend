package com.readour.chat.repository;

import com.readour.chat.entity.ChatPollVote;
import com.readour.chat.entity.ChatPollVoteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatPollVoteRepository extends JpaRepository<ChatPollVote, ChatPollVoteId> {

    List<ChatPollVote> findAllByPollMsgId(Long pollMsgId);
}
