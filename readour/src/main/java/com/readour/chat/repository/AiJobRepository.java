package com.readour.chat.repository;

import com.readour.chat.entity.AiJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiJobRepository extends JpaRepository<AiJob, Long> {

    Optional<AiJob> findTopByDedupeKey(String dedupeKey);
}
