package com.example.eventproject.repository;

import com.example.eventproject.dto.SessionDto;
import com.example.eventproject.model.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EventSessionRepository extends JpaRepository<EventSession, Integer> {

    // ใช้ตอน GET รายละเอียด (ดึงเป็น DTO ตรง ๆ)
    @Query("""
        select new com.example.eventproject.dto.SessionDto(
            s.id, s.name, s.startTime, s.endTime, s.status, s.maxParticipants, s.price
        )
        from EventSession s
        where s.event.id = :eventId
        order by s.startTime asc
        """)
    List<SessionDto> findAllDtosByEventId(Integer eventId);

    // ถ้าบางจุดอยากได้ entity (ไม่ใช่ DTO)
    @Query("""
        select s
        from EventSession s
        where s.event.id = :eventId
        order by s.startTime asc
        """)
    List<EventSession> findByEventId(Integer eventId);

    // ลบทั้งหมดของอีเวนต์ (ใช้ในกรณีอัปเดตแบบ clear & insert ใหม่)
    @Modifying
    @Transactional
    @Query("delete from EventSession s where s.event.id = :eventId")
    void deleteByEventId(Integer eventId);
}
