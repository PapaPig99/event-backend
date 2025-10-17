package com.example.eventproject.repository;

import com.example.eventproject.model.Registration;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {
    // เฉพาะของ user + PAID
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByUserIdAndPaymentStatusOrderByRegisteredAtDesc(
            Integer userId, Registration.PayStatus paymentStatus);

    // ของ user + regStatus + PAID
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByUserIdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
            Integer userId, Registration.RegStatus registrationStatus, Registration.PayStatus paymentStatus);

    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    Optional<Registration> findWithAllRelationsById(Integer id);


    // ทั้งหมดใน event + PAID
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Registration.PayStatus paymentStatus);

    // ทั้งหมดใน event + session + PAID
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndSession_IdAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Integer sessionId, Registration.PayStatus paymentStatus);

    // event + regStatus + PAID
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Registration.RegStatus st, Registration.PayStatus payStatus);

    // event + session + regStatus + PAID
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndSession_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Integer sessionId, Registration.RegStatus registrationStatus, Registration.PayStatus paymentStatus);

    // ลบ Regis ทั้งหมดของ event
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Registration r WHERE r.event.id = :eventId OR r.session.event.id = :eventId")
    int deleteAllByEventCascade(@Param("eventId") Integer eventId);

    // หา active/confirmed (ล็อกแถว) — ไม่จำเป็นต้อง fetch user
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      SELECT r FROM Registration r
       WHERE r.userId = :userId
         AND r.session.id = :sessionId
         AND (
              (r.registrationStatus = 'PENDING' AND r.holdExpiresAt > :now)
              OR r.registrationStatus = 'CONFIRMED'
         )
       ORDER BY r.registeredAt DESC
    """)
    List<Registration> findActiveOrConfirmedForUserAndSession(
            @Param("userId") Integer userId,
            @Param("sessionId") Integer sessionId,
            @Param("now") LocalDateTime now
    );

    // กันแก้ capacity ต่ำกว่ายอดที่จอง/ขายแล้ว
    @Query("""
      SELECT COALESCE(SUM(r.quantity), 0)
      FROM Registration r
      WHERE r.zone.id = :zoneId
        AND r.registrationStatus IN ('PENDING','CONFIRMED')
        AND r.paymentStatus     IN ('UNPAID','PAID')
    """)
    int sumActiveQuantityByZone(@Param("zoneId") Integer zoneId);
}
