package com.example.eventproject.repository;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.model.EventZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository สำหรับจัดการข้อมูลโซน (EventZone)
 * --------------------------------------------------------
 * ใช้ดึงข้อมูลโซนของแต่ละ session เช่น ความจุ, ราคา, จำนวนที่ถูกจอง
 */
@Repository
public interface EventZoneRepository extends JpaRepository<EventZone, Integer> {

    /**
     * ดึงข้อมูลโซนทั้งหมดใน session พร้อมจำนวนที่ถูกจอง และราคา
     * ใช้ในหน้าเลือกโซนหรือดูความจุของรอบ (session)
     *
     * @param sessionId ID ของ session ที่ต้องการดู
     * @return รายการ ZoneAvailabilityDto สำหรับทุกโซนใน session
     */
    @Query("""
        SELECT new com.example.eventproject.dto.ZoneAvailabilityDto(
            z.id,
            z.name,
            z.capacity,
            COUNT(r.id),
            (z.capacity - COUNT(r.id)),
            z.price
        )
        FROM EventZone z
        LEFT JOIN Registration r
          ON r.zone.id = z.id
         AND r.session.id = :sessionId
         AND r.paymentStatus IN ('UNPAID', 'PAID')
        WHERE z.session.id = :sessionId
        GROUP BY z.id, z.name, z.capacity, z.price
        ORDER BY z.id
    """)
    List<ZoneAvailabilityDto> findAvailabilityBySession(@Param("sessionId") Integer sessionId);

    /**
     * ดึง zone ทั้งหมดใน group เดียวกันภายใน session
     * เช่น กลุ่ม VIP, GOLD, SILVER ในรอบเดียวกัน
     *
     * @param sessionId ID ของ session
     * @param groupName ชื่อกลุ่มโซน (group_name)
     */
    List<EventZone> findBySession_IdAndGroupName(Integer sessionId, String groupName);

    /**
     * ดึง zone ทั้งหมดของ session (ใช้ใน service update/delete)
     *
     * @param sessionId ID ของ session
     */
    List<EventZone> findBySession_Id(Integer sessionId);
}
