package com.example.eventproject.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.eventproject.dto.RegistrationDto;
import com.example.eventproject.model.Event;
import com.example.eventproject.model.EventSession;
import com.example.eventproject.model.Registration;
import com.example.eventproject.repository.EventZoneRepository;
import com.example.eventproject.repository.RegistrationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventZoneRepository eventZoneRepository;

    @Transactional
    public Registration create(RegistrationDto.CreateRequest req, Integer userId) {
        if (req.quantity() == null || req.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }

        var zone = eventZoneRepository.findById(req.zoneId())
                .orElseThrow(() -> new IllegalArgumentException("zone not found"));

        // === กันจองซ้ำใน session เดียวกันของผู้ใช้เดียวกัน ===
        var now = LocalDateTime.now();
        var conflicts = registrationRepository.findActiveOrConfirmedForUserAndSession(
                userId, req.sessionId(), now);
        if (!conflicts.isEmpty()) {
            var latest = conflicts.isEmpty() ? null : conflicts.get(0);
            throw new IllegalStateException(
                    "You already have a booking for this session (status=" +
                            latest.getRegistrationStatus() + ", id=" + latest.getId() + ")");
        }

        var reg = new Registration();
        reg.setUserId(userId);
        reg.setEvent(new Event(req.eventId()));
        reg.setSession(new EventSession(req.sessionId()));
        reg.setZone(zone);
        reg.setQuantity(req.quantity());
        reg.setRegistrationStatus(Registration.RegStatus.PENDING);
        reg.setPaymentStatus(Registration.PayStatus.UNPAID);
        reg.setRegisteredAt(now);
        reg.setHoldExpiresAt(now.plusMinutes(10));
        reg.setUnitPrice(zone.getPrice());
        reg.setTotalPrice(zone.getPrice().multiply(BigDecimal.valueOf(req.quantity())));

        return registrationRepository.save(reg);
    }

    @Transactional
    public Registration confirm(Integer id, RegistrationDto.ConfirmRequest req, Integer userId) {
        var reg = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("registration not found"));
        if (!reg.getUserId().equals(userId))
            throw new SecurityException("forbidden");
        if (reg.getRegistrationStatus() != Registration.RegStatus.PENDING)
            throw new IllegalStateException("not PENDING");
        if (reg.getHoldExpiresAt() != null && reg.getHoldExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalStateException("hold expired");

        reg.setRegistrationStatus(Registration.RegStatus.CONFIRMED);
        reg.setPaymentStatus(Registration.PayStatus.PAID);
        reg.setPaymentReference(req.paymentReference());
        reg.setPaidAt(LocalDateTime.now());
        reg.setUpdatedAt(LocalDateTime.now());
        return registrationRepository.save(reg);
    }

    @Transactional
    public Registration cancel(Integer id, RegistrationDto.CancelRequest req, Integer userId) {
        var reg = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("registration not found"));
        if (!reg.getUserId().equals(userId))
            throw new SecurityException("forbidden");
        if (reg.getRegistrationStatus() == Registration.RegStatus.CANCELLED)
            return reg;

        var reason = Registration.CancelledReason.USER_CANCELLED;
        if (req != null && req.reason() != null) {
            try {
                reason = Registration.CancelledReason.valueOf(req.reason());
            } catch (Exception ignored) {
            }
        }
        reg.setRegistrationStatus(Registration.RegStatus.CANCELLED);
        reg.setCancelledReason(reason);
        reg.setUpdatedAt(LocalDateTime.now());
        return registrationRepository.save(reg);
    }

    // user ดู (เฉพาะที่ "จ่ายแล้ว" ทุก event)
    @Transactional(readOnly = true)
    public List<RegistrationDto.Response> getByUserId(Integer userId, String status) {
        List<Registration> regs;
        if (status == null || status.isBlank()) {
            regs = registrationRepository.findByUserIdAndPaymentStatusOrderByRegisteredAtDesc(
                    userId, Registration.PayStatus.PAID);
        } else {
            Registration.RegStatus st;
            try {
                st = Registration.RegStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + status); // หรือ map เป็น 400 ด้วย
                                                                                 // @ControllerAdvice
            }
            regs = registrationRepository.findByUserIdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
                    userId, st, Registration.PayStatus.PAID);
        }
        return regs.stream().map(RegistrationDto.Response::from).toList();
    }

    // Regis ตาม event
    @Transactional(readOnly = true)
    public List<RegistrationDto.Response> getAllByEvent(Integer eventId, String status) {
        List<Registration> regs;
        if (status == null || status.isBlank()) {
            regs = registrationRepository.findByEvent_IdAndPaymentStatusOrderByRegisteredAtDesc(
                    eventId, Registration.PayStatus.PAID);
        } else {
            var st = Registration.RegStatus.valueOf(status.toUpperCase());
            regs = registrationRepository.findByEvent_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
                    eventId, st, Registration.PayStatus.PAID);
        }
        return regs.stream().map(RegistrationDto.Response::from).toList();
    }

    // Regis ตาม event และ session
    @Transactional(readOnly = true)
    public List<RegistrationDto.Response> getAllByEventAndSession(Integer eventId, Integer sessionId, String status) {
        List<Registration> regs;
        if (status == null || status.isBlank()) {
            regs = registrationRepository.findByEvent_IdAndSession_IdAndPaymentStatusOrderByRegisteredAtDesc(
                    eventId, sessionId, Registration.PayStatus.PAID);
        } else {
            var st = Registration.RegStatus.valueOf(status.toUpperCase());
            regs = registrationRepository
                    .findByEvent_IdAndSession_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
                            eventId, sessionId, st, Registration.PayStatus.PAID);
        }
        return regs.stream().map(RegistrationDto.Response::from).toList();
    }

}
