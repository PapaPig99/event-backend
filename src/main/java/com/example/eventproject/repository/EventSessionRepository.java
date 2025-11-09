package com.example.eventproject.repository;

import com.example.eventproject.model.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSessionRepository extends JpaRepository<EventSession, Integer> {

    /**
     * ดึง session ทั้งหมดของ event
     */
    @Query("SELECT s FROM EventSession s WHERE s.event.id = :eventId ORDER BY s.id")
    List<EventSession> findByEventId(@Param("eventId") Integer eventId);

    /**
     * ดึง session พร้อม zones (ใช้ในหน้า detail)
     */
    @Query("SELECT DISTINCT s FROM EventSession s LEFT JOIN FETCH s.zones WHERE s.event.id = :eventId")
    List<EventSession> findWithZonesByEventId(@Param("eventId") Integer eventId);

    /**
     * ลบ session ทั้งหมดของ event (เวลา admin ลบ event)
     */
    void deleteByEvent_Id(Integer eventId);
}
