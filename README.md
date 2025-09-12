# 🐳 Backend Setup (Docker Compose)


ใน `docker-compose.yml` เรามีบริการหลัก ๆ 3 ตัว

| Service           | Description                                           | Port (Host\:Container) |
| ----------------- | ----------------------------------------------------- | ---------------------- |
| **db-mysql**      | ฐานข้อมูล MySQL 8.0 สำหรับเก็บข้อมูล event ทั้งหลาย   | `3308:3306`            |
| **event-backen**  | Backend (Spring Boot / Node / Flask แล้วแต่โปรเจคนี้) | `3137:8080`            |
| **event-fronten** | Frontend (Vue.js) เอาไว้เทสต์เชื่อมกับ backend        | `8081:80`              |

### วิธีรันแบบ Docker
```bash
docker compose up -d --build
# API: http://localhost:3137/api/events
# WEB: http://localhost:8081
# DB:  127.0.0.1:3308 (itds323/itds323)
```

### ล้างและ rebuild ใหม่หมด
```bash
docker compose down -v
docker compose up -d --build
```

### 💥 หยุดทุก container

```bash
docker compose down
```

---


## 💡 Tips

* ถ้าแก้โค้ด backend แล้วไม่เห็นเปลี่ยน ลอง `docker compose down && docker compose up --build -d`

---



### 📁 Mock up Data 
#### Table events
```bash
INSERT INTO `events`
(`id`,`title`,`description`,`category`,`location`,
 `start_date`,`end_date`,`status`,
 `sale_start_at`,`sale_end_at`,`sale_until_soldout`,`door_open_time`,
 `poster_image_url`,`detail_image_url`,`seatmap_image_url`,
 `created_by_user_id`,`created_at`,`updated_at`)
VALUES
/* ============ CONCERT (5) ============ */
(2001,'MARIAH CAREY The Celebration of Mimi',
 'คอนเสิร์ตเต็มรูปแบบของ “ดิว่าแห่งสหัสวรรษ” กลับมาโชว์ในกรุงเทพฯ',
 'Concert','Impact Arena, Muang Thong Thani, Bangkok',
 '2025-07-19','2025-07-19','OPEN',
 '2025-05-01 10:00:00',NULL,1,'18:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0523/6523/mariah-carey-5935c3a85948f-l.jpg',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0523/6523/mariah-carey-5935c3a85948f-l.jpg',
 NULL,1,NOW(),NOW()),

(2002,'Doja Cat “Ma Vie” World Tour – Bangkok',
 'Doja Cat เปิดทัวร์เอเชีย ปักหมุดกรุงเทพฯ',
 'Concert','IMPACT Exhibition Hall 5–6, Muang Thong Thani, Bangkok',
 '2025-12-18','2025-12-18','OPEN',
 '2025-08-30 10:00:00',NULL,1,'18:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0546/6546/doja-cat-ma-vie-world-tour-68c2a8c728f4a-l.jpg',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0546/6546/doja-cat-ma-vie-world-tour-68c2a8c728f4a-l.jpg',
 NULL,1,NOW(),NOW()),

(2003,'DAY6 10th Anniversary Tour <The DECADE> in BANGKOK',
 'DAY6 ฉลองครบรอบ 10 ปี กับโชว์ใหญ่ที่อิมแพ็ค อารีน่า',
 'Concert','Impact Arena, Muang Thong Thani, Bangkok',
 '2025-09-27','2025-09-27','OPEN',
 '2025-07-27 10:00:00',NULL,1,'18:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0537/6537/day6-10th-anniversary-tour-the-decade-in-bangkok-68b1f37c58ce3-l.jpg',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0537/6537/day6-10th-anniversary-tour-the-decade-in-bangkok-68b1f37c58ce3-l.jpg',
 NULL,1,NOW(),NOW()),

(2004,'YUURI ASIA TOUR 2025 IN BANGKOK',
 'เจ้าของเพลงฮิต “Dry Flower” ทัวร์เอเชีย แวะไทยที่ One Bangkok',
 'Concert','One Bangkok Forum, Bangkok',
 '2025-09-28','2025-09-28','OPEN',
 '2025-08-01 10:00:00',NULL,1,'17:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0540/6540/yuuri-asia-tour-2025-in-bangkok-68b3f903f33c1-l.jpg',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0540/6540/yuuri-asia-tour-2025-in-bangkok-68b3f903f33c1-l.jpg',
 NULL,1,NOW(),NOW()),

(2005,'The Smashing Pumpkins – Rock Invasion 2025 – Asia Tour – Bangkok',
 'ตำนานอัลเทอร์เนทีฟร็อคเยือนไทยที่ Union Hall',
 'Concert','Union Hall, Union Mall, Bangkok',
 '2025-10-01','2025-10-01','OPEN',
 '2025-07-20 10:00:00',NULL,1,'18:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0541/6541/the-smashing-pumpkins-rock-invasion-2025-asia-tour-bangkok-68b4220b8b2a2-l.jpg',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0541/6541/the-smashing-pumpkins-rock-invasion-2025-asia-tour-bangkok-68b4220b8b2a2-l.jpg',
 NULL,1,NOW(),NOW()),

/* ============ PERFORMING ARTS / การแสดง (5) ============ */
(2101,'THE LION INSIDE (Bangkok Theatre Project)',
 'มิวสิคัลสำหรับครอบครัวจากสหราชอาณาจักร เล่นที่ M Theatre',
 'Performing Arts','M Theatre, Phetchaburi Rd., Bangkok',
 '2025-11-20','2025-11-23','OPEN',
 '2025-08-22 10:00:00',NULL,1,'09:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0451/6451/the-lion-inside-68a3f988dda6e-l.png',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0451/6451/the-lion-inside-68a3f988dda6e-l.png',
 NULL,1,NOW(),NOW()),

(2102,'Bangkok City Ballet July Performance 2025: Cinderella',
 'บัลเลต์เต็มองก์ “ซินเดอเรลลา” โดย Bangkok City Ballet',
 'Performing Arts','Thailand Cultural Centre, Main Hall, Bangkok',
 '2025-07-12','2025-07-12','CLOSED',
 '2025-06-14 10:00:00',NULL,0,'17:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0375/6375/bangkok-city-ballet-july-performance-2025-ballet-cinderella-684ceafda1948-l.png',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0375/6375/bangkok-city-ballet-july-performance-2025-ballet-cinderella-684ceafda1948-l.png',
 NULL,1,NOW(),NOW()),

(2103,'“KABUKI Otokodate Hana No Yoshiwara” by Ichikawa Danjuro XIII in Bangkok 2025',
 'คาบูกิระดับตำนานจากญี่ปุ่น แสดงที่เมืองไทยรัชดาลัย เธียเตอร์',
 'Performing Arts','Muangthai Rachadalai Theatre, Bangkok',
 '2025-12-13','2025-12-14','OPEN',
 '2025-09-06 10:00:00',NULL,1,'15:00:00',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0465/6465/kabuki-otokodate-hana-no-yoshiwara-by-ichikawa-danjuro-xlll-in-bangkok-2025-68b7db4fd8df2-l.png',
 'https://www.thaiticketmajor.com/img_poster/prefix_1/0465/6465/kabuki-otokodate-hana-no-yoshiwara-by-ichikawa-danjuro-xlll-in-bangkok-2025-68b7db4fd8df2-l.png',
 NULL,1,NOW(),NOW()),

(2104,'Disney On Ice presents “Find Your Hero”',
 'โชว์ไอซ์สเก็ตสำหรับครอบครัว',
 'Performing Arts','Impact Arena, Muang Thong Thani, Bangkok',
 '2025-03-27','2025-03-30','CLOSED',
 '2025-02-01 10:00:00',NULL,0,'17:30:00',
 /* โปสเตอร์จากหน้าอีเวนต์ DOIP (ตรวจได้บนเพจ) */
 'https://www.thaiticketmajor.com/disneyonice2025/assets/og.jpg',
 'https://www.thaiticketmajor.com/disneyonice2025/assets/og.jpg',
 NULL,1,NOW(),NOW()),

(2105,'Bangkok’s 27th International Festival of Dance & Music',
 'เทศกาลการแสดงนานาชาติของกรุงเทพฯ',
 'Performing Arts','Thailand Cultural Centre, Bangkok',
 '2025-09-06','2025-10-15','CLOSED',
 '2025-06-15 10:00:00',NULL,0,'19:00:00',
 'https://www.ticdfestival.com/wp-content/uploads/2025/06/Bangkok-IFDM-2025-Poster.jpg',
 'https://www.ticdfestival.com/wp-content/uploads/2025/06/Bangkok-IFDM-2025-Poster.jpg',
 NULL,1,NOW(),NOW()),

/* ============ EDUCATION / การศึกษา (5) ============ */
(2201,'Techsauce Global Summit 2025',
 'ซัมมิตเทค/สตาร์ทอัพที่ใหญ่สุดในภูมิภาค',
 'Education','Queen Sirikit National Convention Center (QSNCC), Bangkok',
 '2025-08-21','2025-08-22','OPEN',
 '2025-04-15 10:00:00',NULL,1,'09:00:00',
 'https://summit.techsauce.co/2025/og/tsgs25-og.jpg',
 'https://summit.techsauce.co/2025/og/tsgs25-og.jpg',
 NULL,1,NOW(),NOW()),

(2202,'AWS Summit Bangkok 2025',
 'งานคลาวด์ใหญ่ของ AWS รวมคีย์โน้ต/บูท/เวิร์กช็อป',
 'Education','Queen Sirikit National Convention Center (QSNCC), Bangkok',
 '2025-04-29','2025-04-29','CLOSED',
 '2025-03-15 10:00:00',NULL,1,'08:00:00',
 'https://d1.awsstatic.com/events/summits/2025/Bangkok/AWS-Summit-Bangkok-2025.2f9a5c3c0f7.png',
 'https://d1.awsstatic.com/events/summits/2025/Bangkok/AWS-Summit-Bangkok-2025.2f9a5c3c0f7.png',
 NULL,1,NOW(),NOW()),

(2203,'Bitkub Summit 2025',
 'มหกรรมบล็อกเชนและ Web3',
 'Education','QSNCC Hall 3–4, Bangkok',
 '2025-10-25','2025-10-26','OPEN',
 '2025-07-15 10:00:00',NULL,1,'09:00:00',
 'https://media.nationthailand.com/uploads/images/contents/w1024/2025/05/hpoqWzmnQ1QD7SjFl3s9.webp',
 'https://media.nationthailand.com/uploads/images/contents/w1024/2025/05/hpoqWzmnQ1QD7SjFl3s9.webp',
 NULL,1,NOW(),NOW()),

(2204,'Data Demystified Summit Bangkok 2025',
 'งานคอนเฟอเรนซ์ด้าน Customer/Data Analytics',
 'Education','Hotel Nikko Bangkok, Fuji Grand Ballroom, Bangkok',
 '2025-10-07','2025-10-07','OPEN',
 '2025-08-01 10:00:00',NULL,1,'08:30:00',
 'https://datademystifiedsummit.com/wp-content/uploads/2021/11/DD-Summit-Logo-White-1.png',
 'https://datademystifiedsummit.com/wp-content/uploads/2021/11/DD-Summit-Logo-White-1.png',
 NULL,1,NOW(),NOW()),

(2205,'PyCon Thailand 2025',
 'คอนเฟอเรนซ์ Python ชุมชนโอเพ่นซอร์สในไทย',
 'Education','Bangkok (venue TBA)',
 '2025-11-15','2025-11-16','OPEN',
 '2025-08-31 10:00:00',NULL,1,'09:00:00',
 'https://pyconthailand.org/static/og/pyconth-2025.png',
 'https://pyconthailand.org/static/og/pyconth-2025.png',
 NULL,1,NOW(),NOW()),

/* ============ BUSINESS & INVESTMENT / ธุรกิจและการลงทุน (5) ============ */
(2301,'Money Expo Bangkok 2025',
 'งานมหกรรมการเงินประจำปี',
 'Business & Investment','IMPACT Challenger Hall, Muang Thong Thani, Bangkok',
 '2025-05-15','2025-05-18','CLOSED',
 '2025-03-01 10:00:00',NULL,1,'10:00:00',
 'https://www.moneyexpo.net/wp-content/uploads/2025/04/money-expo-2025-bkk.jpg',
 'https://www.moneyexpo.net/wp-content/uploads/2025/04/money-expo-2025-bkk.jpg',
 NULL,1,NOW(),NOW()),

(2302,'SET Investment Expo 2025',
 'งานลงทุนจากตลาดหลักทรัพย์ฯ',
 'Business & Investment','QSNCC, Bangkok',
 '2025-09-05','2025-09-08','OPEN',
 '2025-06-20 10:00:00',NULL,1,'10:00:00',
 'https://www.set.or.th/files/2025/09/set-investment-expo-2025.jpg',
 'https://www.set.or.th/files/2025/09/set-investment-expo-2025.jpg',
 NULL,1,NOW(),NOW()),

(2303,'Thailand International Franchise & Business Opportunities (TFBO) 2025',
 'แฟรนไชส์/SME ใหญ่สุดในภูมิภาค',
 'Business & Investment','BITEC Bangna, Bangkok',
 '2025-07-10','2025-07-13','CLOSED',
 '2025-05-20 10:00:00',NULL,1,'10:00:00',
 'https://www.biztradeshows.org/wp-content/uploads/2025/05/tfbo-2025.jpg',
 'https://www.biztradeshows.org/wp-content/uploads/2025/05/tfbo-2025.jpg',
 NULL,1,NOW(),NOW()),

(2304,'The Standard Economic Forum 2025',
 'ฟอรัมเศรษฐกิจประจำปีโดย The Standard',
 'Business & Investment','Bangkok (venue TBA)',
 '2025-11-29','2025-11-30','OPEN',
 '2025-09-15 10:00:00',NULL,1,'09:00:00',
 'https://thestandard.co/wp-content/uploads/2025/10/the-standard-economic-forum-2025.jpg',
 'https://thestandard.co/wp-content/uploads/2025/10/the-standard-economic-forum-2025.jpg',
 NULL,1,NOW(),NOW()),

(2305,'Startup Thailand x Innovation Thailand Expo 2025',
 'อีเวนต์นวัตกรรมและสตาร์ทอัพ',
 'Business & Investment','QSNCC, Bangkok',
 '2025-10-03','2025-10-05','OPEN',
 '2025-08-15 10:00:00',NULL,1,'10:00:00',
 'https://nsti.thaigov.go.th/wp-content/uploads/2025/08/ite-2025.jpg',
 'https://nsti.thaigov.go.th/wp-content/uploads/2025/08/ite-2025.jpg',
 NULL,1,NOW(),NOW()),

/* ============ SPORTS / กีฬา (5) ============ */
(2401,'Amazing Thailand Marathon Bangkok 2025',
 'มาราธอนรายการใหญ่ประจำกรุงเทพฯ',
 'Sports','Rajamangala National Stadium & Bangkok city course',
 '2025-03-23','2025-03-23','CLOSED',
 '2024-12-15 10:00:00',NULL,1,'03:30:00',
 'https://www.amazingthailandmarathon.com/2025/og/atm-bkk-2025.jpg',
 'https://www.amazingthailandmarathon.com/2025/og/atm-bkk-2025.jpg',
 NULL,1,NOW(),NOW()),

(2402,'Bangkok Marathon 2025 (Standard Chartered Bangkok Marathon)',
 'รายการมาราธอนไอคอนิกริมแม่น้ำเจ้าพระยา',
 'Sports','Sanam Chai – Memorial Bridge – Bangkok old town',
 '2025-11-16','2025-11-16','OPEN',
 '2025-07-01 10:00:00',NULL,1,'02:30:00',
 'https://www.bkkmarathon.com/images/2025/bkk-marathon-2025.jpg',
 'https://www.bkkmarathon.com/images/2025/bkk-marathon-2025.jpg',
 NULL,1,NOW(),NOW()),

(2403,'Pattaya Marathon 2025',
 'พัทยามาราธอนริมทะเล',
 'Sports','Pattaya Beach Road, Chonburi',
 '2025-07-21','2025-07-21','CLOSED',
 '2025-04-01 10:00:00',NULL,1,'03:30:00',
 'https://www.pattayamarathon.go.th/images/2025/pattaya-marathon-2025.jpg',
 'https://www.pattayamarathon.go.th/images/2025/pattaya-marathon-2025.jpg',
 NULL,1,NOW(),NOW()),

(2404,'Phuket Marathon 2025 (Laguna Phuket Marathon)',
 'สนามมาราธอนยอดนิยมของนักวิ่งต่างชาติ',
 'Sports','Laguna Phuket, Phuket',
 '2025-06-08','2025-06-08','CLOSED',
 '2025-03-01 10:00:00',NULL,1,'04:00:00',
 'https://www.phuketmarathon.com/wp-content/uploads/2025/02/laguna-phuket-marathon-2025.jpg',
 'https://www.phuketmarathon.com/wp-content/uploads/2025/02/laguna-phuket-marathon-2025.jpg',
 NULL,1,NOW(),NOW()),

(2405,'Supersports 10 Mile International Run Bangkok 2025',
 'ซีรีส์วิ่ง 10 ไมล์ กลางกรุงเทพฯ',
 'Sports','CentralWorld & Ratchaprasong area, Bangkok',
 '2025-05-11','2025-05-11','CLOSED',
 '2025-02-15 10:00:00',NULL,1,'04:30:00',
 'https://supersports10mile.com/wp-content/uploads/2025/03/supersports-10-mile-bangkok-2025.jpg',
 'https://supersports10mile.com/wp-content/uploads/2025/03/supersports-10-mile-bangkok-2025.jpg',
 NULL,1,NOW(),NOW());

```
