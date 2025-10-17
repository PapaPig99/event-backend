-- Users / Roles
INSERT INTO users (id, email, password, name, created_at, updated_at)
VALUES (1, 'admin@test.com', '{noop}password', 'Administrator', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (id, code, name) VALUES
                                       (1,'ADMIN','Administrator'),
                                       (2,'USER','General User');

INSERT INTO user_roles (user_id, role_id, created_at)
VALUES (1, 1, CURRENT_TIMESTAMP);

-- Event #1
INSERT INTO events (
    id, title, description, category, location,
    start_date, end_date, status,
    sale_start_at, sale_end_at, sale_until_soldout,
    door_open_time,
    poster_image_url, detail_image_url, seatmap_image_url,
    created_by_user_id, created_at, updated_at
) VALUES (
             1,
             'MARIAH CAREY The Celebration of Mimi',
             'คอนเสิร์ตฉลองอัลบั้ม The Emancipation of Mimi พร้อมโชว์พิเศษจาก Mariah Carey',
             'concert',
             'อิมแพค ชาเลนเจอร์ ฮอลล์ เมืองทองธานี',
             DATE '2025-10-11', DATE '2025-10-12',
             'OPEN',
             TIMESTAMP '2025-07-19 10:00:00', NULL, TRUE,
             'ก่อนเริ่มงาน 1 ชม.',
             '/images/poster_mariah.jpg', NULL, '/images/seatmap_mariah.jpg',
             1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
         );

-- Sessions (id ชัดเจน)
INSERT INTO event_sessions (id, event_id, name, start_time, status, created_at, updated_at) VALUES
                                                                                                (101, 1, 'รอบแรก 11 ต.ค.', TIME '19:00:00', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                (102, 1, 'รอบสอง 12 ต.ค.', TIME '20:00:00', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Zones (id ชัดเจน)
INSERT INTO event_zones (id, event_id, name, capacity, price, created_at, updated_at) VALUES
                                                                                          (1001, 1, 'Zone A ที่นั่งติดเวทีสุด', 180, 12000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                          (1002, 1, 'Zone B ที่นั่งเวที',       220,  6500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Event #2
INSERT INTO events (
    id, title, description, category, location,
    start_date, end_date, status,
    sale_start_at, sale_end_at, sale_until_soldout,
    door_open_time,
    poster_image_url, detail_image_url, seatmap_image_url,
    created_by_user_id, created_at, updated_at
) VALUES (
             2,
             'ONE LUMPINEE',
             'การแข่งขันมวยไทย + MMA ประจำสัปดาห์',
             'sport',
             'สนามมวยเวทีลุมพินี กรุงเทพฯ',
             DATE '2025-09-05', DATE '2025-09-05',
             'CLOSED',
             TIMESTAMP '2025-06-01 19:00:00', NULL, TRUE,
             'ก่อนเริ่มงาน 1 ชม.',
             '/images/poster_onelumpinee.jpg', NULL, NULL,
             1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
         );

INSERT INTO event_sessions (id, event_id, name, start_time, status, created_at, updated_at) VALUES
    (201, 2, 'คู่เอก ศุกร์ที่ 5 ก.ย.', TIME '19:30:00', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO event_zones (id, event_id, name, capacity, price, created_at, updated_at) VALUES
    (2001, 2, 'Ringside', 200, 3500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
