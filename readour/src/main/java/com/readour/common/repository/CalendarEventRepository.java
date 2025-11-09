package com.readour.common.repository;

import com.readour.common.entity.CalendarEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    Optional<CalendarEvent> findByEventIdAndCalendarIdAndIsDeletedFalse(Long eventId, Long calendarId);

    boolean existsByCalendarIdAndTitleAndStartsAtAndEndsAtAndIsDeletedFalse(Long calendarId,
                                                                             String title,
                                                                             LocalDateTime startsAt,
                                                                             LocalDateTime endsAt);

    List<CalendarEvent> findAllByCalendarIdInAndIsDeletedFalseAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
            Collection<Long> calendarIds, LocalDateTime rangeEnd, LocalDateTime rangeStart, Sort sort);

    Optional<CalendarEvent> findByEventIdAndCreatedByAndIsDeletedFalse(Long eventId, Long createdBy);
}