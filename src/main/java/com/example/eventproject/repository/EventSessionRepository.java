package com.example.eventproject.repository;

import com.example.eventproject.dto.SessionDto;
import com.example.eventproject.model.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EventSessionRepository extends JpaRepository<EventSession, Integer> {

    @Query("""
        select s
        from EventSession s
        where s.event.id = :eventId
        order by s.startTime asc
        """)
    List<EventSession> findByEventId(Integer eventId);

    // ลบทั้งหมดของอีเวนต์ id
    @Modifying
    @Transactional
    @Query("delete from EventSession s where s.event.id = :eventId")
    void deleteByEventId(Integer eventId);

}
