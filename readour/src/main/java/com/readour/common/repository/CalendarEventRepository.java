package com.readour.common.repository;

import com.readour.common.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    Optional<CalendarEvent> findByEventIdAndCalendarIdAndIsDeletedFalse(Long eventId, Long calendarId);

    boolean existsByCalendarIdAndTitleAndStartsAtAndEndsAtAndIsDeletedFalse(Long calendarId,
                                                                             String title,
                                                                             LocalDateTime startsAt,
                                                                             LocalDateTime endsAt);
}
