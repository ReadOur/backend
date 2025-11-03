package com.readour.chat.repository;

import com.readour.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            select r
            from ChatRoom r
            where r.isActive = true
              and upper(r.scope) = 'PUBLIC'
              and (
                    :query is null
                    or :query = ''
                    or lower(r.name) like lower(concat('%', :query, '%'))
                    or lower(coalesce(r.description, '')) like lower(concat('%', :query, '%'))
                  )
            """)
    Page<ChatRoom> findActivePublicRooms(@Param("query") String query, Pageable pageable);
}
