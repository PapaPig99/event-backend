package com.example.eventproject.repository;

import com.example.eventproject.dto.ZoneDto;
import com.example.eventproject.model.EventZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EventZoneRepository extends JpaRepository<EventZone, Integer> {

    // ใช้ตอน GET รายละเอียด (ดึงเป็น DTO ตรง ๆ)
    @Query("""
        select new com.example.eventproject.dto.ZoneDto(
            z.id, z.name, z.capacity, z.price
        )
        from EventZone z
        where z.event.id = :eventId
        order by z.name asc
        """)
    List<ZoneDto> findAllDtosByEventId(Integer eventId);

    // ถ้าบางจุดอยากได้ entity (ไม่ใช่ DTO)
    @Query("""
        select z
        from EventZone z
        where z.event.id = :eventId
        order by z.name asc
        """)
    List<EventZone> findByEventId(Integer eventId);

    // ลบทั้งหมดของอีเวนต์ (ใช้ในกรณีอัปเดตแบบ clear & insert ใหม่)
    @Modifying
    @Transactional
    @Query("delete from EventZone z where z.event.id = :eventId")
    void deleteByEventId(Integer eventId);
}
