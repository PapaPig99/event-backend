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






}
