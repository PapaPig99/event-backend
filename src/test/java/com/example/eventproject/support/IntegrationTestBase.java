package com.example.eventproject.support;

import com.example.eventproject.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class IntegrationTestBase {

    @DynamicPropertySource
    static void forceTestDatasource(DynamicPropertyRegistry r) {
        // ปิด dotenv เพื่อไม่ให้ .env ไปทับค่า
        r.add("spring.dotenv.enabled", () -> "false");

        // บังคับใช้ H2 เสมอ
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:eventdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");

        // ให้ Hibernate สร้างตาราง แล้วค่อย init data.sql
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.defer-datasource-initialization", () -> "true");
        r.add("spring.sql.init.mode", () -> "always");

        // (เลือกได้) เปิดลอกเพื่อยืนยันว่าใช้ H2 จริง
        r.add("logging.level.com.zaxxer.hikari", () -> "DEBUG");
        r.add("spring.datasource.hikari.pool-name", () -> "H2Pool");
    }
}
