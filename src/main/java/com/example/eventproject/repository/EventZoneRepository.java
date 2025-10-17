package com.example.eventproject.repository;

import com.example.eventproject.dto.PriceDto;
import org.springframework.data.repository.query.Param;
import com.example.eventproject.model.EventZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import com.example.eventproject.dto.ZoneAvailabilityDto;


import java.util.List;

public interface EventZoneRepository extends JpaRepository<EventZone, Integer> {

    @Modifying
    @Transactional
    @Query("delete from EventZone z where z.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Integer eventId);

    @Query("""
    SELECT new com.example.eventproject.dto.ZoneAvailabilityDto(
      z.id,
      z.name,
      z.capacity,
      COALESCE(SUM(r.quantity), 0),
      (z.capacity - COALESCE(SUM(r.quantity), 0))
    )
    FROM EventZone z
      JOIN EventSession s
           ON s.id = :sessionId
          AND s.event = z.event
      LEFT JOIN Registration r
           ON r.zone = z
          AND r.session = s
          AND r.registrationStatus IN ('PENDING','CONFIRMED')
          AND r.paymentStatus     IN ('UNPAID','PAID')
    GROUP BY z.id, z.name, z.capacity
    ORDER BY z.id
    """)
    List<ZoneAvailabilityDto> findAvailabilityBySession(@Param("sessionId") Integer sessionId);

    @Query("""
        select new com.example.eventproject.dto.PriceDto(z.price)
        from EventZone z
        where z.event.id = :eventId
    """)
    List<PriceDto> findPricesByEventId(@Param("eventId") Integer eventId);

    List<EventZone> findByEventId(Integer eventId);

}

