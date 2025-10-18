package com.example.eventproject.support;

import com.example.eventproject.EventProjectApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


@SpringBootTest(classes = EventProjectApplication.class)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
    @DynamicPropertySource
    static void h2(DynamicPropertyRegistry r) {
        r.add("spring.dotenv.enabled", () -> "false");
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:eventdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        r.add("spring.jpa.defer-datasource-initialization", () -> "true");
        r.add("spring.sql.init.mode", () -> "always");   // ✅ ให้ data.sql ถูกโหลดอัตโนมัติ 1 รอบ
    }
}

