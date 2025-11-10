package com.example.eventproject.dto;

import com.example.eventproject.model.Registration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RegistrationDto {

    /* ==========================================================
       CREATE REQUEST — ใช้สำหรับ POST /api/registrations
       สร้างหลายใบใน zone เดียว (แต่จ่ายครั้งเดียว)
       ========================================================== */
    public record CreateRequest(
            Integer eventId,
            Integer sessionId,
            Integer zoneId,
            Integer quantity // จำนวนที่ผู้ใช้ต้องการจอง
    ) {}

    /* ==========================================================
       CREATE RESPONSE — หลังจากจองสำเร็จ
       แสดงผลรวมทั้งหมดใน order เดียว
       ========================================================== */
    public record CreateResponse(
            String paymentReference,
            Integer eventId,
            Integer sessionId,
            Integer zoneId,
            String zoneName,
            BigDecimal pricePerTicket,
            Integer quantity,
            BigDecimal totalPrice,
            String paymentStatus,
            List<String> ticketCodes
    ) {}

    /* ==========================================================
       INDIVIDUAL TICKET RESPONSE — ใช้แสดงแต่ละใบ
       ========================================================== */
    public record Response(
            Integer id,
            String email,
            Integer eventId,
            Integer sessionId,
            Integer zoneId,
            String zoneName,
            String paymentReference,
            String paymentStatus,
            BigDecimal price,
            BigDecimal totalPrice,
            LocalDateTime paidAt,
            String ticketCode,
            Boolean isCheckedIn,
            LocalDateTime checkedInAt,
            LocalDateTime createdAt
    ) {
        public static Response from(Registration reg) {
            return new Response(
                    reg.getId(),
                    reg.getEmail(),
                    reg.getEvent() != null ? reg.getEvent().getId() : null,
                    reg.getSession() != null ? reg.getSession().getId() : null,
                    reg.getZone() != null ? reg.getZone().getId() : null,
                    reg.getZone() != null ? reg.getZone().getName() : null,
                    reg.getPaymentReference(),
                    reg.getPaymentStatus() != null ? reg.getPaymentStatus().name() : null,
                    reg.getPrice(),
                    reg.getTotalPrice(),
                    reg.getPaidAt(),
                    reg.getTicketCode(),
                    reg.getIsCheckedIn(),
                    reg.getCheckedInAt(),
                    reg.getCreatedAt()
            );
        }
    }

    /* ==========================================================
       CONFIRM PAYMENT — ใช้สำหรับ PATCH /api/registrations/confirm
       ========================================================== */
    public record ConfirmRequest(
            String paymentReference
    ) {}

    public record ConfirmResponse(
            String paymentReference,
            String paymentStatus,
            LocalDateTime paidAt,
            BigDecimal totalPrice
    ) {}
}
