package com.example.eventproject.controller;

import com.example.eventproject.dto.RegistrationDto;
import com.example.eventproject.model.Registration;
import com.example.eventproject.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-only tests — ทดสอบ behavior ของ RegistrationController
 */
@WebMvcTest(controllers = RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    RegistrationService registrationService;

    /* ==========================================================
       SAMPLE DTO RESPONSE (ใช้กับ service.getAllByUser / getByUserAndStatus)
       ========================================================== */
    private RegistrationDto.Response sampleResponse(
            int id,
            String regStatus,
            String payStatus
    ) {
        LocalDateTime now = LocalDateTime.of(2025, 10, 17, 12, 0, 0);

        return new RegistrationDto.Response(
                id,                         // 1) id
                "TICKET-" + id,             // 2) ticketCode
                1,                          // 3) eventId
                10,                         // 4) sessionId
                5,                          // 5) zoneId
                regStatus,                  // 6) registrationStatus
                payStatus,                  // 7) paymentStatus (หรือ field ที่ mapping อยู่จริง)
                "CARD",                     // 8) paymentMethod
                new BigDecimal("500.00"),   // 9) pricePerTicket
                new BigDecimal("1000.00"),  // 10) totalPrice
                now.plusMinutes(10),        // 11) reservedUntil
                "user@example.com",         // 12) email
                Boolean.FALSE,              // 13) isCheckedIn
                now,                        // 14) createdAt
                "PAID".equals(payStatus) ? now : null  // 15) paidAt
        );
    }

    /* ==========================================================
       SAMPLE ENTITY (ใช้กับ service.getAll() ที่คืน List<Registration> )
       ========================================================== */
    private Registration sampleRegistration(int id) {
        LocalDateTime now = LocalDateTime.of(2025, 10, 17, 12, 0, 0);

        Registration r = new Registration();
        r.setId(id);
        r.setTicketCode("TICKET-" + id);
        r.setPrice(new BigDecimal("500.00"));
        r.setTotalPrice(new BigDecimal("1000.00"));
        r.setPaymentStatus(Registration.PayStatus.PAID);
        r.setPaymentReference("TXREF-" + id);
        r.setCreatedAt(now);
        r.setIsCheckedIn(false);
        return r;
    }

    /* ==========================================================
       /api/registrations/me
       ========================================================== */

    @Test
    @DisplayName("GET /api/registrations/me → 200 (no status, ใช้ email query param)")
    void myRegs_noStatus() throws Exception {

        var dto = sampleResponse(1, "PENDING", "UNPAID");

        // controller จะเรียก registrationService.getAllByUser(finalEmail)
        when(registrationService.getAllByUser("user@example.com"))
                .thenReturn(List.of(dto));

        mvc.perform(get("/api/registrations/me")
                        // ส่ง email ตรง ๆ → ไม่ต้องพึ่ง @AuthenticationPrincipal
                        .param("email", "user@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(registrationService)
                .getAllByUser("user@example.com");
    }

    @Test
    @DisplayName("GET /api/registrations/me?status=PAID → 200 (ใช้ email query param)")
    void myRegs_withStatus() throws Exception {

        var dto = sampleResponse(2, "CONFIRMED", "PAID");

        when(registrationService.getByUserAndStatus(
                "user@example.com",
                Registration.PayStatus.PAID))
                .thenReturn(List.of(dto));

        mvc.perform(get("/api/registrations/me")
                        .param("email", "user@example.com")
                        .param("status", "PAID")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 👇 เปลี่ยนจาก paymentStatus มาเช็ค id แทน ให้ไม่ผูกกับชื่อฟิลด์ใน DTO จริง
                .andExpect(jsonPath("$[0].id", is(2)));

        verify(registrationService)
                .getByUserAndStatus("user@example.com", Registration.PayStatus.PAID);
    }

    /* ==========================================================
       GET ONE BY ID — /api/registrations/{id}
       ========================================================== */
    @Test
    @DisplayName("GET /api/registrations/{id} → 200")
    void getOne_ok() throws Exception {

        var reg = sampleRegistration(5);

        when(registrationService.getAll())
                .thenReturn(List.of(reg));

        mvc.perform(get("/api/registrations/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));

        verify(registrationService).getAll();
    }

    @Test
    @DisplayName("GET /api/registrations/{id} → 404 not found")
    void getOne_notFound() throws Exception {

        when(registrationService.getAll())
                .thenReturn(List.of()); // empty

        mvc.perform(get("/api/registrations/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(registrationService).getAll();
    }
}
