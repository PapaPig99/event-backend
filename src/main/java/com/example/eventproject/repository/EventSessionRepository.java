package com.example.eventproject.repository;

import com.example.eventproject.dto.SessionDto;
import com.example.eventproject.model.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventSessionRepository extends JpaRepository<EventSession, Integer> {

    @Query("""
        select new com.example.eventproject.dto.SessionDto(
            s.id, s.name, s.startTime, s.endTime, s.status, s.maxParticipants, s.price
        )
        from EventSession s
        where s.event.id = :eventId
        order by s.startTime asc
        """)
    List<SessionDto> findAllDtosByEventId(Integer eventId);
}
