package com.example.eventproject.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.example.eventproject.model.Event;

public interface DashboardRepository extends Repository<Event, Long> {

    // นับ event ที่ status = 'OPEN'
    @Query(value = "SELECT COUNT(*) FROM events e WHERE e.status='OPEN'", nativeQuery = true)
    long countActiveEvents();

    // รวมจำนวนตั๋วที่ payment_status = 'PAID'
    @Query(value = "SELECT COALESCE(SUM(r.quantity),0) FROM registrations r WHERE r.payment_status='PAID'", nativeQuery = true)
    long sumTicketsSold();

    // ✅ รวมจำนวน registration ทั้งหมด (ทุกสถานะ)
    @Query(value = "SELECT COALESCE(SUM(r.quantity),0) FROM registrations r", nativeQuery = true)
    long sumTotalRegistrations();

    // projection สำหรับตาราง sales progress
    interface EventSalesRow {
        Integer getEventId();
        String getTitle();
        String getCategory();
        Long getCapacity();
        Long getSold();
    }

    @Query(value = """
        SELECT 
          e.id        AS eventId,
          e.title     AS title,
          e.category  AS category,
          COALESCE(z.cap, 0)  AS capacity,
          COALESCE(s.sold, 0) AS sold
        FROM events e
        LEFT JOIN (
            SELECT event_id, SUM(capacity) AS cap
            FROM event_zones
            GROUP BY event_id
        ) z ON z.event_id = e.id
        LEFT JOIN (
            SELECT event_id, SUM(quantity) AS sold
            FROM registrations
            WHERE payment_status='PAID'
            GROUP BY event_id
        ) s ON s.event_id = e.id
        ORDER BY e.created_at DESC
        """, nativeQuery = true)
    List<EventSalesRow> findSalesRows();
}
