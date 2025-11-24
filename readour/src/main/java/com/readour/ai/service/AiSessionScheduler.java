package com.readour.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSessionScheduler {

    private final AiService aiService;

    /**
     * 주기적으로 진행 중인 세션의 신규 메시지를 슬라이스 요약한다.
     * interval은 ai.session.summary.interval-ms, initialDelay는 ai.session.summary.initial-delay-ms로 조정 가능하다.
     */
    @Scheduled(fixedDelayString = "${ai.session.summary.interval-ms:60000}",
            initialDelayString = "${ai.session.summary.initial-delay-ms:10000}")
    public void runSessionSummaries() {
        try {
            aiService.runScheduledSessionSummaries();
        } catch (Exception ex) {
            log.error("세션 슬라이스 자동 요약 스케줄러 실행 중 오류", ex);
        }
    }
}
