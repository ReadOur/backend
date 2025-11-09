package com.readour.common.repository;

import com.readour.common.entity.Calendar;
import com.readour.common.enums.CalendarScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    Optional<Calendar> findByScopeAndRelatedRoomId(CalendarScope scope, Long relatedRoomId);

    Optional<Calendar> findFirstByOwnerUserIdAndScope(Long ownerUserId, CalendarScope scope);
    List<Calendar> findByScopeAndRelatedRoomIdIn(CalendarScope scope, Collection<Long> roomIds);
}
