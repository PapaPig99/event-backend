package com.example.eventproject.dto;

import com.example.eventproject.model.Registration;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegistrationDto {

    /* ---------- CREATE ---------- */
    // ใช้สำหรับ POST /api/registrations
    public record CreateRequest(
            Integer eventId,
            Integer sessionId,
            Integer zoneId,
            Integer quantity,       // กรณีโซนไม่มี seat number
            Integer seatNumber      // กรณีโซนมี seat number
    ) {}

    /* ---------- RESPONSE ---------- */
    public record Response(
            Integer id,
            String email,
            Integer eventId,
            Integer sessionId,
            Integer zoneId,
            String zoneName,
            Integer seatNumber,
            Integer quantity,
            String paymentStatus,
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
                    reg.getSeatNumber(),
                    reg.getQuantity(),
                    reg.getPaymentStatus() != null ? reg.getPaymentStatus().name() : null,
                    reg.getTotalPrice(),
                    reg.getPaidAt(),
                    reg.getTicketCode(),
                    reg.getIsCheckedIn(),
                    reg.getCheckedInAt(),
                    reg.getCreatedAt()
            );
        }
    }

    /* ---------- CONFIRM ---------- */
    // ใช้สำหรับ PATCH /api/registrations/{id}/confirm
    public record ConfirmRequest(
            String paymentReference
    ) {}

}
