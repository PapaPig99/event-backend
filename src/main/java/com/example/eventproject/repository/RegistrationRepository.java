package com.example.eventproject.repository;

import com.example.eventproject.model.Registration;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository สำหรับจัดการข้อมูลการจองตั๋ว (Registration)
 * ------------------------------------------------------------
 * ใช้เชื่อมกับฐานข้อมูลผ่าน Spring Data JPA
 * มีทั้ง query พื้นฐาน, query แบบ custom, และ aggregation
 * ครอบคลุมทั้งฝั่งผู้ใช้และผู้ดูแลระบบ
 */
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    /* ==========================================================
        ดึงข้อมูลการจอง
       ========================================================== */

    /**
     * ดึงรายการจองทั้งหมดของผู้ใช้ (ตามอีเมล)
     * ใช้ในหน้า "My Tickets" ของผู้ใช้ เพื่อแสดงทุก order ที่เคยจอง
     *
     * @param email อีเมลของผู้ใช้
     * @return รายการ Registration ทั้งหมด เรียงจากใหม่ → เก่า
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEmailOrderByCreatedAtDesc(String email);

    /**
     * ดึงรายการจองของผู้ใช้ที่อยู่ในสถานะที่กำหนด
     * เช่น ใช้ดูเฉพาะรายการที่ “ยังไม่จ่าย” หรือ “จ่ายแล้ว”
     *
     * @param email อีเมลของผู้ใช้
     * @param paymentStatus สถานะการชำระเงิน (UNPAID / PAID)
     * @return รายการ Registration ที่ตรงกับสถานะ
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEmailAndPaymentStatusOrderByCreatedAtDesc(
            String email, Registration.PayStatus paymentStatus);

    /**
     * ดึงรายการจองทั้งหมดของอีเวนต์หนึ่ง (แยกตามสถานะการชำระเงิน)
     * ใช้ฝั่ง Admin เพื่อดูยอดจองหรือยอดจ่ายของอีเวนต์นั้น
     *
     * @param eventId ID ของอีเวนต์
     * @param paymentStatus สถานะการชำระเงิน
     * @return รายการ Registration ของอีเวนต์นั้น
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndPaymentStatusOrderByCreatedAtDesc(
            Integer eventId, Registration.PayStatus paymentStatus);

    /**
     * ดึงรายการจองในอีเวนต์ + รอบ (session) ที่กำหนด
     * ใช้ในหน้า “รายชื่อผู้จองในรอบนั้น” เช่น คอนเสิร์ตวัน/รอบหนึ่ง
     *
     * @param eventId ID ของอีเวนต์
     * @param sessionId ID ของรอบ (session)
     * @param paymentStatus สถานะการชำระเงิน
     * @return รายการ Registration ของรอบนั้น
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndSession_IdAndPaymentStatusOrderByCreatedAtDesc(
            Integer eventId, Integer sessionId, Registration.PayStatus paymentStatus);

    /**
     * ดึงข้อมูลการจองจากรหัสตั๋ว (ticketCode)
     * ใช้สำหรับหน้า "ตรวจตั๋ว / Check-in" หรือ validate ตั๋วก่อนเข้า
     *
     * @param ticketCode รหัสตั๋วที่ unique
     * @return Registration ของตั๋วนั้น (ถ้ามี)
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    Optional<Registration> findByTicketCode(String ticketCode);

    /**
     * ดึงรายการจองทั้งหมดที่อยู่ในออเดอร์เดียวกัน (paymentReference เดียว)
     * ใช้ในขั้นตอน confirm payment หรือสรุปบิลรวม
     *
     * @param paymentReference รหัสอ้างอิงการชำระเงิน
     * @return รายการ Registration ทั้งหมดในชุดเดียวกัน
     */
    List<Registration> findByPaymentReference(String paymentReference);

    /* ==========================================================
       DELETE — ลบข้อมูล
       ========================================================== */

    /**
     * ลบการจองทั้งหมดของอีเวนต์ (ใช้เวลาลบ Event)
     * Cascade แบบ manual เพื่อให้ข้อมูล Registration ถูกลบออกด้วย
     *
     * @param eventId ID ของอีเวนต์ที่ต้องการลบ
     * @return จำนวนแถวที่ถูกลบ
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Registration r WHERE r.event.id = :eventId")
    int deleteAllByEventCascade(@Param("eventId") Integer eventId);

    /* ==========================================================
       AGGREGATION — สรุปยอด / ตรวจสอบจำนวน
       ========================================================== */

    /**
     * นับจำนวนตั๋วที่ “ชำระเงินแล้ว” ใน zone หนึ่ง
     * ใช้ดูยอดขายจริงของแต่ละโซน (เฉพาะที่จ่ายแล้ว)
     *
     * @param zoneId ID ของโซน
     * @return จำนวนตั๋วที่ชำระแล้ว
     */
    @Query("""
      SELECT COUNT(r.id)
      FROM Registration r
      WHERE r.zone.id = :zoneId
        AND r.paymentStatus = 'PAID'
    """)
    int countPaidByZone(@Param("zoneId") Integer zoneId);

    /**
     * นับจำนวนการจองทั้งหมดในโซน (รวม UNPAID + PAID)
     * ใช้เพื่อตรวจสอบว่าโซนเต็มหรือยัง ก่อนอนุญาตให้จองเพิ่ม
     *
     * @param zoneId ID ของโซน
     * @return จำนวนที่ถูกจองรวมทั้งหมด
     */
    @Query("""
      SELECT COUNT(r.id)
      FROM Registration r
      WHERE r.zone.id = :zoneId
        AND r.paymentStatus IN ('UNPAID', 'PAID')
    """)
    int countAllBookedInZone(@Param("zoneId") Integer zoneId);
}
