package com.example.eventproject.controller;

import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RegistrationControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;

    @Test
    void createRegistration_success() throws Exception {
        String body = """
                  {"eventId":1,"sessionId":101,"zoneId":1001,"quantity":2}
                """;


        mvc.perform(post("/api/registrations")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated()); // ถ้าโปรเจกต์คืน 200 ให้เปลี่ยนเป็น .isOk()
}


        @Test
    void overBooking_409() throws Exception {
        String body = """
          {"eventId":1,"sessionId":101,"zoneId":1001,"quantity":9999}
        """;

        mvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
