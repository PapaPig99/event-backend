package com.example.eventproject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.eventproject.model.Registration;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    /* ==========================================================
       ดึงข้อมูลแบบโหลดความสัมพันธ์ครบ
       ========================================================== */
    @EntityGraph(attributePaths = {"user", "user.role", "event", "session", "zone"})
    @Query("SELECT r FROM Registration r ORDER BY r.createdAt DESC")
    List<Registration> findAllWithAllRelations();

    /* ==========================================================
        ดึงรายการจองทั้งหมดของผู้ใช้ (ตามอีเมล)
       ========================================================== */

    @EntityGraph(attributePaths = {"user", "user.role", "event", "session", "zone"})
    List<Registration> findByEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {"user", "user.role", "event", "session", "zone"})
    List<Registration> findByEmailAndPaymentStatusOrderByCreatedAtDesc(
            String email,
            Registration.PayStatus paymentStatus
    );

    /* ==========================================================
       ดึงรายการจองตาม EVENT
       ========================================================== */

    @EntityGraph(attributePaths = {"user", "user.role", "event", "session", "zone"})
    List<Registration> findByEvent_IdOrderByCreatedAtDesc(Integer eventId);

    /* ==========================================================
       ดึงรายการจองตาม EVENT + SESSION
       ========================================================== */

    @EntityGraph(attributePaths = {"user", "user.role", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndSession_IdOrderByCreatedAtDesc(
            Integer eventId,
            Integer sessionId
    );

    /* ==========================================================
       CHECK-IN → ticket by code ตาม event และ session
       ========================================================== */
    @EntityGraph(attributePaths = {"user", "user.role", "event", "session", "zone"})
    Optional<Registration> findByTicketCode(String ticketCode);

    /* ==========================================================
       PAYMENT GROUP → by paymentReference
       ========================================================== */
    List<Registration> findByPaymentReference(String paymentReference);

    /* ==========================================================
       DELETE all from event
       ========================================================== */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Registration r WHERE r.event.id = :eventId")
    int deleteAllByEventCascade(@Param("eventId") Integer eventId);

    /* ==========================================================
       AGGREGATION → ตรวจสอบจำนวนตั๋วในโซน
       ========================================================== */
    @Query("""
      SELECT COUNT(r.id)
      FROM Registration r
      WHERE r.zone.id = :zoneId
        AND r.paymentStatus IN ('UNPAID', 'PAID')
    """)
    int countAllBookedInZone(@Param("zoneId") Integer zoneId);
}
