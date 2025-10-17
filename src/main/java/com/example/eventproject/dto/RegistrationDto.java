package com.example.eventproject.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.eventproject.model.Registration;

public class RegistrationDto {

    // POST /api/registrations
    public record CreateRequest(
            Integer eventId,
            Integer sessionId,
            Integer zoneId,
            Integer quantity
    ) {}

    // Response DTO
    public record Response(
            Integer id,
            Integer userId,
            Integer eventId,
            Integer sessionId,
            Integer zoneId,
            Integer quantity,
            String registrationStatus,
            String paymentStatus,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            LocalDateTime holdExpiresAt,
            LocalDateTime registeredAt,
            LocalDateTime updatedAt,
            LocalDateTime paidAt,
            String paymentReference,
            String cancelledReason,
            UserDto user
    ) {
        // แปลงจาก Entity -> DTO
        public static Response from(Registration reg) {
            return new Response(
                    reg.getId(),
                    reg.getUserId(), // ถ้ามี relation reg.getUser() ก็ยังคงเก็บ userId แยกได้
                    reg.getEvent() != null ? reg.getEvent().getId() : null,
                    reg.getSession() != null ? reg.getSession().getId() : null,
                    reg.getZone() != null ? reg.getZone().getId() : null,
                    reg.getQuantity(),
                    reg.getRegistrationStatus() != null ? reg.getRegistrationStatus().name() : null,
                    reg.getPaymentStatus() != null ? reg.getPaymentStatus().name() : null,
                    reg.getUnitPrice(),
                    reg.getTotalPrice(),
                    reg.getHoldExpiresAt(),
                    reg.getRegisteredAt(),
                    reg.getUpdatedAt(),
                    reg.getPaidAt(),
                    reg.getPaymentReference(),
                    reg.getCancelledReason() != null ? reg.getCancelledReason().name() : null,
                    UserDto.from(reg.getUser())
            );
        }
    }

    // PATCH /api/registrations/{id}/confirm
    public record ConfirmRequest(
            String paymentReference
    ) {}

    // PATCH /api/registrations/{id}/cancel
    public record CancelRequest(
            String reason
    ) {}
}
