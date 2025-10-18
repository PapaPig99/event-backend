package com.example.eventproject.controller;

import com.example.eventproject.config.CurrentUser;
import com.example.eventproject.dto.RegistrationDto;
import com.example.eventproject.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Controller-only tests — ปิด security filter และ mock Service */
@WebMvcTest(controllers = RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RegistrationControllerTest.WebConfig.class) // <-- ใช้ WebConfig ใหม่
class RegistrationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om; // ใช้ของ Spring Boot (มี JavaTimeModule แล้ว)

    @MockBean RegistrationService registrationService;

    /** ===== Register @AuthenticationPrincipal resolver เข้า MVC ===== */
    @TestConfiguration
    static class WebConfig implements WebMvcConfigurer {

        @Bean
        HandlerMethodArgumentResolver currentUserResolver() {
            return new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                            && CurrentUser.class.isAssignableFrom(parameter.getParameterType());
                }
                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    return new CurrentUser(
                            123L,
                            "user@example.com",
                            "Tester",
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                }
            };
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(currentUserResolver()); // <-- สำคัญ: register เข้า MVC
        }

        // ❌ อย่า override ObjectMapper ตรงนี้ ปล่อยให้ Spring Boot สร้างเอง
        // ถ้าจำเป็นจริง ๆ ให้ลง JavaTimeModule ด้วย แต่ในเคสนี้ไม่ต้อง
    }

    /** ===== helper: Response ตัวอย่างสำหรับ asserts ===== */
    private RegistrationDto.Response sampleResponse(int id, String regStatus, String payStatus) {
        LocalDateTime now = LocalDateTime.of(2025, 10, 17, 12, 0, 0);
        return new RegistrationDto.Response(
                id,                  // id
                123,                 // userId
                1,                   // eventId
                10,                  // sessionId
                5,                   // zoneId
                regStatus,           // registrationStatus  ✅ มาก่อน quantity
                2,                   // quantity
                payStatus,           // paymentStatus
                "CARD",              // paymentMethodOrChannel (เติมให้ครบ)
                new BigDecimal("500.00"),   // pricePerTicket
                new BigDecimal("1000.00"),  // totalPrice
                now.plusMinutes(10), // reservedUntil
                now,                 // createdAt
                now,                 // updatedAt
                "PAID".equals(payStatus) ? now : null, // paidAt
                "TXREF-123",         // txRef
                null,                // cancelReason
                null                 // user (UserDto) — ไม่ใช้ก็ใส่ null
        );
    }


    /* ========== CREATE ========== */
    @Test
    @DisplayName("POST /api/registrations :: 201 + mapping ผ่าน Response.from + verify service.create(req, 123)")
    void create_shouldReturn201_andJson() throws Exception {
        String createJson = """
            { "eventId": 1, "sessionId": 10, "zoneId": 5, "quantity": 2 }
        """;

        var fakeReg = Mockito.mock(com.example.eventproject.model.Registration.class);
        Mockito.when(registrationService.create(any(RegistrationDto.CreateRequest.class), eq(123)))
                .thenReturn(fakeReg);

        var dto = sampleResponse(999, "PENDING", "UNPAID");

        try (MockedStatic<RegistrationDto.Response> mocked =
                     Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(fakeReg)).thenReturn(dto);

            mvc.perform(post("/api/registrations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(999)))
                    .andExpect(jsonPath("$.userId", is(123)))
                    .andExpect(jsonPath("$.eventId", is(1)))
                    .andExpect(jsonPath("$.sessionId", is(10)))
                    .andExpect(jsonPath("$.zoneId", is(5)))
                    .andExpect(jsonPath("$.quantity", is(2)))
                    .andExpect(jsonPath("$.registrationStatus", is("PENDING")))
                    .andExpect(jsonPath("$.paymentStatus", is("UNPAID")));

            verify(registrationService, times(1))
                    .create(any(RegistrationDto.CreateRequest.class), eq(123));
        }
    }

    /* ========== CONFIRM ========== */
    @Test
    @DisplayName("PATCH /api/registrations/{id}/confirm :: 200 + mapping ผ่าน Response.from + verify service.confirm(id, req, 123)")
    void confirm_shouldReturn200_andJson() throws Exception {
        Integer regId = 77;
        String confirmJson = """
    { "paymentReference": "TX-OK-777" }
    """;


        var fakeReg = Mockito.mock(com.example.eventproject.model.Registration.class);
        Mockito.when(registrationService.confirm(eq(regId), any(RegistrationDto.ConfirmRequest.class), eq(123)))
                .thenReturn(fakeReg);

        var dto = sampleResponse(regId, "CONFIRMED", "PAID");

        try (MockedStatic<RegistrationDto.Response> mocked =
                     Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(fakeReg)).thenReturn(dto);

            mvc.perform(patch("/api/registrations/{id}/confirm", regId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(confirmJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(regId)))
                    .andExpect(jsonPath("$.registrationStatus", is("CONFIRMED")))
                    .andExpect(jsonPath("$.paymentStatus", is("PAID")));

            verify(registrationService, times(1))
                    .confirm(eq(regId), any(RegistrationDto.ConfirmRequest.class), eq(123));
        }
    }

    /* ========== CANCEL ========== */
    @Test
    @DisplayName("PATCH /api/registrations/{id}/cancel (no body) :: 200 + mapping ผ่าน Response.from + verify req=null")
    void cancel_noBody_shouldReturn200_andJson() throws Exception {
        Integer regId = 88;

        var fakeReg = Mockito.mock(com.example.eventproject.model.Registration.class);
        Mockito.when(registrationService.cancel(eq(regId), isNull(), eq(123)))
                .thenReturn(fakeReg);

        var dto = sampleResponse(regId, "CANCELLED", "UNPAID");

        try (MockedStatic<RegistrationDto.Response> mocked =
                     Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(fakeReg)).thenReturn(dto);

            mvc.perform(patch("/api/registrations/{id}/cancel", regId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(regId)))
                    .andExpect(jsonPath("$.registrationStatus", is("CANCELLED")));

            verify(registrationService, times(1))
                    .cancel(eq(regId), isNull(), eq(123));
        }
    }

    @Test
    @DisplayName("PATCH /api/registrations/{id}/cancel (with body) :: 200 + mapping ผ่าน Response.from + verify req!=null")
    void cancel_withBody_shouldReturn200_andJson() throws Exception {
        Integer regId = 89;
        String cancelJson = """
{
  "reason": "user request"
}
""";


        var fakeReg = Mockito.mock(com.example.eventproject.model.Registration.class);
        Mockito.when(registrationService.cancel(eq(regId), any(RegistrationDto.CancelRequest.class), eq(123)))
                .thenReturn(fakeReg);

        var dto = sampleResponse(regId, "CANCELLED", "UNPAID");

        try (MockedStatic<RegistrationDto.Response> mocked =
                     Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(fakeReg)).thenReturn(dto);

            mvc.perform(patch("/api/registrations/{id}/cancel", regId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cancelJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(regId)))
                    .andExpect(jsonPath("$.registrationStatus", is("CANCELLED")));

            verify(registrationService, times(1))
                    .cancel(eq(regId), any(RegistrationDto.CancelRequest.class), eq(123));
        }
    }

    /* ========== MY REGS ========== */
    @Test
    @DisplayName("GET /api/registrations/me (no status) :: 200 + verify getByUserId(123, null)")
    void myRegs_noStatus() throws Exception {
        var dto = sampleResponse(1, "PENDING", "UNPAID");
        Mockito.when(registrationService.getByUserId(eq(123), isNull()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/api/registrations/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(registrationService, times(1)).getByUserId(eq(123), isNull());
    }

    @Test
    @DisplayName("GET /api/registrations/me?status=PAID :: 200 + verify getByUserId(123, \"PAID\")")
    void myRegs_withStatus() throws Exception {
        var dto = sampleResponse(2, "CONFIRMED", "PAID");
        Mockito.when(registrationService.getByUserId(eq(123), eq("PAID")))
                .thenReturn(List.of(dto));

        mvc.perform(get("/api/registrations/me").param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentStatus", is("PAID")));

        verify(registrationService, times(1)).getByUserId(eq(123), eq("PAID"));
    }

    /* ========== LIST BY EVENT ========== */
    @Test
    @DisplayName("GET /api/registrations/event/{eventId} :: 200 + verify getAllByEvent(eventId, null)")
    void getAllByEvent_noStatus() throws Exception {
        Integer eventId = 1001;
        var dto = sampleResponse(10, "CONFIRMED", "PAID");

        Mockito.when(registrationService.getAllByEvent(eq(eventId), isNull()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/api/registrations/event/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].paymentStatus", is("PAID")));

        verify(registrationService, times(1)).getAllByEvent(eq(eventId), isNull());
    }

    @Test
    @DisplayName("GET /api/registrations/event/{eventId}?status=PAID :: 200 + verify getAllByEvent(eventId, \"PAID\")")
    void getAllByEvent_withStatus() throws Exception {
        Integer eventId = 1002;
        var dto = sampleResponse(11, "CONFIRMED", "PAID");

        Mockito.when(registrationService.getAllByEvent(eq(eventId), eq("PAID")))
                .thenReturn(List.of(dto));

        mvc.perform(get("/api/registrations/event/{eventId}", eventId)
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentStatus", is("PAID")));

        verify(registrationService, times(1)).getAllByEvent(eq(eventId), eq("PAID"));
    }
}
