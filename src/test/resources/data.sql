-- USERS: ตัด created_at / updated_at ทิ้ง
INSERT INTO users (id, email, password, name)
VALUES (1, 'admin@test.com', '{noop}password', 'Administrator');

-- ROLES: ถ้า entity มีตาราง roles ตามนี้ ใช้ได้ (ถ้าไม่มี role ไม่เกี่ยวกับ /api/events ก็ลบทิ้งได้)
INSERT INTO roles (id, code, name) VALUES
                                       (1,'ADMIN','Administrator'),
                                       (2,'USER','General User');

-- USER_ROLES: ส่วนใหญ่ join table จะมีแค่ user_id, role_id (ตัด created_at ทิ้ง)
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);

-- EVENTS: ใส่เฉพาะฟิลด์ที่ entity บังคับ NOT NULL
INSERT INTO events (
    id, title, description, category, location,
    start_date, end_date, status,
    sale_start_at, sale_until_soldout,
    poster_image_url, created_by_user_id
) VALUES (
             1,
             'MARIAH CAREY The Celebration of Mimi',
             'คอนเสิร์ตฉลองอัลบั้ม The Emancipation of Mimi พร้อมโชว์พิเศษจาก Mariah Carey',
             'concert',
             'อิมแพค ชาเลนเจอร์ ฮอลล์ เมืองทองธานี',
             DATE '2025-10-11', DATE '2025-10-12',
             'OPEN',
             TIMESTAMP '2025-07-19 10:00:00', TRUE,
             '/images/poster_mariah.jpg',
             1
         );

-- EVENT SESSIONS: ตัด created_at/updated_at ถ้า entity ไม่มี
INSERT INTO event_sessions (id, event_id, name, start_time, status)
VALUES
    (101, 1, 'รอบแรก 11 ต.ค.', TIME '19:00:00', 'OPEN'),
    (102, 1, 'รอบสอง 12 ต.ค.', TIME '20:00:00', 'OPEN');

-- EVENT ZONES: เช่นเดียวกัน ใส่เฉพาะที่จำเป็น
INSERT INTO event_zones (id, event_id, name, capacity, price)
VALUES
    (1001, 1, 'Zone A ที่นั่งติดเวทีสุด', 180, 12000),
    (1002, 1, 'Zone B ที่นั่งเวที',       220,  6500);
