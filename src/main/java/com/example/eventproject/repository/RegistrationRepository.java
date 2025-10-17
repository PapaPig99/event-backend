package com.example.eventproject.repository;

import com.example.eventproject.model.Registration;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {
    // เฉพาะของ user + event + จ่ายแล้ว
    List<Registration> findByUserIdAndPaymentStatusOrderByRegisteredAtDesc(
            Integer userId, Registration.PayStatus paymentStatus);

    // ของ user + event + status การจอง + จ่ายแล้ว
    List<Registration> findByUserIdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
            Integer userId, Registration.RegStatus registrationStatus, Registration.PayStatus paymentStatus);


    //เฉพาะที่จ่ายแล้วทั้งหมดใน event
    List<Registration> findByEvent_IdAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Registration.PayStatus paymentStatus);

    //เฉพาะที่จ่ายแล้วทั้งหมดใน event + session
    List<Registration> findByEvent_IdAndSession_IdAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Integer sessionId, Registration.PayStatus paymentStatus);

    //จ่ายแล้ว + กรอง registrationStatus ด้วย (เช่น CONFIRMED) ตาม event
    List<Registration> findByEvent_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Registration.RegStatus st, Registration.PayStatus payStatus);

    //จ่ายแล้ว + กรอง registrationStatus ด้วย (เช่น CONFIRMED) ตาม event + session
    List<Registration> findByEvent_IdAndSession_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
            Integer eventId, Integer sessionId, Registration.RegStatus registrationStatus, Registration.PayStatus paymentStatus);

    //ลบRegisทั้งหมดที่ event.id = eventId และ session.event.id
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Registration r WHERE r.event.id = :eventId OR r.session.event.id = :eventId")
    int deleteAllByEventCascade(@Param("eventId") Integer eventId);

    //ถ้ามี PENDING ที่ยังไม่หมดเวลา (holdExpiresAt > now) → ไม่ให้สร้าง
    //หรือมี CONFIRMED ใน session เดียวกันแล้ว → ไม่ให้สร้างซ้ำ
    //ถ้าเป็น CANCELLED หรือ PENDING ที่หมดเวลาแล้ว → อนุญาตให้สร้างใหม่ได้
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

    // กันการแก้event ที่แก้ capacity ต่ำกว่ายอดที่ “จอง/ขายแล้ว”
    @Query("""
  SELECT COALESCE(SUM(r.quantity), 0)
  FROM Registration r
  WHERE r.zone.id = :zoneId
    AND r.registrationStatus IN ('PENDING','CONFIRMED')
    AND r.paymentStatus     IN ('UNPAID','PAID')
""")
    int sumActiveQuantityByZone(@Param("zoneId") Integer zoneId);


}
