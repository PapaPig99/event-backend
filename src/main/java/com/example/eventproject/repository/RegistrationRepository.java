package com.example.eventproject.repository;

import com.example.eventproject.model.Registration;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository สำหรับจัดการข้อมูลการลงทะเบียน (Registration)
 * ใช้เชื่อมกับฐานข้อมูลผ่าน JPA โดยมีทั้ง query พื้นฐานและ query แบบ custom
 */
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    /* ===================== BASIC FIND ===================== */

    /**
     * 🔹 ดึงรายการจองทั้งหมดของผู้ใช้ (ตามอีเมล)
     * ใช้ EntityGraph เพื่อดึงข้อมูลสัมพันธ์ (user, event, session, zone) มาพร้อมกัน
     * @param email อีเมลผู้ใช้
     * @return รายการ Registration ทั้งหมดของผู้ใช้นั้น เรียงจากเวลาล่าสุด -> เก่าสุด
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEmailOrderByCreatedAtDesc(String email);

    /**
     * 🔹 ดึงรายการจองของผู้ใช้ (เฉพาะสถานะการจ่ายเงินที่ระบุ)
     * เช่น ใช้ดูเฉพาะที่ชำระเงินแล้ว หรือยังไม่ชำระ
     * @param email อีเมลผู้ใช้
     * @param paymentStatus สถานะการชำระเงิน (UNPAID หรือ PAID)
     * @return รายการ Registration ของผู้ใช้ในสถานะนั้น
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEmailAndPaymentStatusOrderByCreatedAtDesc(
            String email, Registration.PayStatus paymentStatus);

    /**
     * 🔹 ดึงรายการจองทั้งหมดของอีเวนต์ที่ระบุ (เฉพาะสถานะที่กำหนด)
     * ใช้ดูยอดผู้จอง/ชำระแล้วในแต่ละอีเวนต์
     * @param eventId ID ของอีเวนต์
     * @param paymentStatus สถานะการชำระเงิน
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndPaymentStatusOrderByCreatedAtDesc(
            Integer eventId, Registration.PayStatus paymentStatus);

    /**
     * 🔹 ดึงรายการจองในอีเวนต์และรอบ (session) ที่กำหนด
     * เช่น ใช้ดูรายชื่อผู้เข้าร่วมในรอบเวลาใดเวลาหนึ่งของคอนเสิร์ต
     * @param eventId ID ของอีเวนต์
     * @param sessionId ID ของรอบ (session)
     * @param paymentStatus สถานะการชำระเงิน
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    List<Registration> findByEvent_IdAndSession_IdAndPaymentStatusOrderByCreatedAtDesc(
            Integer eventId, Integer sessionId, Registration.PayStatus paymentStatus);

    /**
     * 🔹 ดึงข้อมูลการจองจากรหัสตั๋ว (ticketCode)
     * ใช้ตอนผู้ใช้กรอกรหัสตั๋วเพื่อตรวจสอบข้อมูล
     * @param ticketCode รหัสตั๋วที่ unique
     */
    @EntityGraph(attributePaths = {"user", "event", "session", "zone"})
    Optional<Registration> findByTicketCode(String ticketCode);


    /* ===================== DELETE ===================== */

    /**
     * 🔹 ลบการจองทั้งหมดของอีเวนต์ (ใช้เวลาลบ Event)
     * Cascade แบบ manual เพื่อให้ข้อมูล Registration ของอีเวนต์นั้นถูกลบออกทั้งหมด
     * @param eventId ID ของอีเวนต์
     * @return จำนวนแถวที่ถูกลบ
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Registration r WHERE r.event.id = :eventId")
    int deleteAllByEventCascade(@Param("eventId") Integer eventId);


    /* ===================== AGGREGATION ===================== */

    /**
     * 🔹 นับจำนวนผู้ที่ชำระเงินแล้วใน zone นั้น ๆ
     * ใช้เพื่อดูยอดขายจริง (เฉพาะที่จ่ายแล้ว)
     * @param zoneId ID ของโซน
     * @return จำนวนที่ชำระแล้ว
     */
    @Query("""
      SELECT COUNT(r.id)
      FROM Registration r
      WHERE r.zone.id = :zoneId
        AND r.paymentStatus = 'PAID'
    """)
    int countPaidByZone(@Param("zoneId") Integer zoneId);

    /**
     * 🔹 ตรวจสอบว่าหมายเลขที่นั่ง (seatNumber) ใน zone ถูกจองแล้วหรือไม่
     * ใช้ตอนเลือกที่นั่งแบบมีหมายเลข (hasSeatNumbers = true)
     * @param zoneId ID ของโซน
     * @param seatNumber หมายเลขที่นั่ง
     * @return จำนวนรายการที่จองหมายเลขนี้แล้ว (ถ้ามากกว่า 0 แสดงว่าถูกจองไปแล้ว)
     */
    @Query("""
      SELECT COUNT(r.id)
      FROM Registration r
      WHERE r.zone.id = :zoneId
        AND r.seatNumber = :seatNumber
        AND r.paymentStatus IN ('UNPAID', 'PAID')
    """)
    int countExistingSeat(@Param("zoneId") Integer zoneId, @Param("seatNumber") Integer seatNumber);

    /**
     * 🔹 นับจำนวนที่ถูกจองทั้งหมดใน zone (รวมทั้ง UNPAID และ PAID)
     * ใช้ตรวจสอบความจุว่าเต็มหรือยัง ก่อนอนุญาตให้จองเพิ่ม
     * @param zoneId ID ของโซน
     * @return จำนวนที่จองรวมทั้งหมดใน zone นั้น
     */
    @Query("""
      SELECT COUNT(r.id)
      FROM Registration r
      WHERE r.zone.id = :zoneId
        AND r.paymentStatus IN ('UNPAID', 'PAID')
    """)
    int countAllBookedInZone(@Param("zoneId") Integer zoneId);
}
