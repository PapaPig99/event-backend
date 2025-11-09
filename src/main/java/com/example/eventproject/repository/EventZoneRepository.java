package com.example.eventproject.repository;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.model.EventZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventZoneRepository extends JpaRepository<EventZone, Integer> {

    /**
     * ดึงข้อมูลโซนทั้งหมดใน session พร้อมจำนวนที่ถูกจอง และราคา
     */
    @Query("""
    SELECT new com.example.eventproject.dto.ZoneAvailabilityDto(
        z.id,
        z.name,
        z.capacity,
        COUNT(r.id),
        (z.capacity - COUNT(r.id)),
        false,    
        NULL,
        z.price
    )
    FROM EventZone z
    LEFT JOIN Registration r
        ON r.zone.id = z.id
       AND r.session.id = :sessionId
       AND r.paymentStatus IN ('UNPAID','PAID')
    WHERE z.session.id = :sessionId
    GROUP BY z.id, z.name, z.capacity, z.price
    ORDER BY z.id
""")
    List<ZoneAvailabilityDto> findAvailabilityBySession(Integer sessionId);



    /**
     * ดึงหมายเลขที่นั่งที่ถูกจองแล้วใน session หนึ่ง
     */
    @Query("""
        SELECT r.seatNumber
        FROM Registration r
        WHERE r.session.id = :sessionId
          AND r.paymentStatus IN ('UNPAID','PAID')
          AND r.seatNumber IS NOT NULL
        ORDER BY r.seatNumber
    """)
    List<Integer> findBookedSeatNumbers(@Param("sessionId") Integer sessionId);


    /**
     * ดึง zone ทั้งหมดใน group เดียวกันภายใน session
     */
    List<EventZone> findBySession_IdAndGroupName(Integer sessionId, String groupName);

    /**
     * ดึง zone ทั้งหมดของ session (ใช้ใน service update/delete)
     */
    List<EventZone> findBySession_Id(Integer sessionId);
}
