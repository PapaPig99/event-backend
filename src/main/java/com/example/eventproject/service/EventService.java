package com.example.eventproject.service;

import com.example.eventproject.dto.*;
import com.example.eventproject.model.*;
import com.example.eventproject.repository.EventRepository;
import com.example.eventproject.repository.EventSessionRepository;
import com.example.eventproject.repository.EventZoneRepository;
import com.example.eventproject.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepo;
    private final EventSessionRepository sessionRepo;
    private final EventZoneRepository zoneRepo;
    private final FileStorageService fileStorageService;
    private final RegistrationRepository registrationRepository;
    /* ========== READ ========== */

    @Transactional(readOnly = true)
    public List<EventSummaryView> list() {
        return eventRepo.findAllByOrderByStartDateAsc();
    }

    @Transactional(readOnly = true)
    public EventDetailDto get(Integer id) {
        Event e = eventRepo.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        var sessions = e.getSessions().stream()
                .map(s -> new SessionDto(
                        s.getId(), s.getName(), s.getStartTime(),
                        s.getStatus()
                )).toList();

        var zones = e.getZones().stream()
                .map(z -> new ZoneDto(z.getId(), z.getName(), z.getCapacity(), z.getPrice()))
                .toList();

        return new EventDetailDto(
                e.getId(), e.getTitle(), e.getCategory(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.getStatus(),
                e.getSaleStartAt(), e.getSaleEndAt(), e.isSaleUntilSoldout(),
                e.getDoorOpenTime(),e.getDescription(), e.getPosterImageUrl(), e.getDetailImageUrl(),
                e.getSeatmapImageUrl(), sessions, zones
        );
    }

    /* ========== CREATE ========== */

    @Transactional
    public Integer create(EventUpsertRequest dto,
                          MultipartFile poster, MultipartFile detail, MultipartFile seatmap,Integer userId) {
        Event e = new Event();
        applyCoreFields(e, dto);
        setImagesFromUploadsOrDto(e, dto, poster, detail, seatmap);

            // กำหนดuserที่สร้างeventจาก token
        e.setCreatedByUserId(userId);

        Event saved = eventRepo.save(e);

        if (dto.sessions() != null) {
            for (SessionDto s : dto.sessions()) {
                EventSession es = new EventSession();
                es.setEvent(saved);
                es.setName(s.name());
                es.setStartTime(s.startTime());
                es.setStatus(s.status());
                sessionRepo.save(es);
            }
        }

        if (dto.zones() != null) {
            for (ZoneDto z : dto.zones()) {
                EventZone ez = new EventZone();
                ez.setEvent(saved);
                ez.setName(z.name());
                ez.setCapacity(z.capacity());
                ez.setPrice(z.price());
                zoneRepo.save(ez);
            }
        }

        return saved.getId();
    }

    /* ========== UPDATE ========== */
    @Transactional
    public void update(Integer id, EventUpsertRequest dto,
                       MultipartFile poster, MultipartFile detail, MultipartFile seatmap) {
        Event e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        // 1) core fields + images
        applyCoreFields(e, dto);
        setImagesFromUploadsOrDto(e, dto, poster, detail, seatmap);
        eventRepo.save(e);

        // 2) SESSIONS
        var existingSessions = sessionRepo.findByEventId(id);
        var sessionById = existingSessions.stream()
                .collect(java.util.stream.Collectors.toMap(EventSession::getId, s -> s));

        if (dto.sessions() != null) {
            for (SessionDto s : dto.sessions()) {
                if (s.id() != null && sessionById.containsKey(s.id())) {
                    var es = sessionById.get(s.id());
                    es.setName(s.name());
                    es.setStartTime(s.startTime());
                    es.setStatus(s.status());
                    sessionRepo.save(es);
                } else {
                    var es = new EventSession();
                    es.setEvent(e);
                    es.setName(s.name());
                    es.setStartTime(s.startTime());
                    es.setStatus(s.status());
                    sessionRepo.save(es);
                }
            }
        }

        // 3) ZONES
        var existingZones = zoneRepo.findByEventId(id);
        var zoneById = existingZones.stream()
                .collect(java.util.stream.Collectors.toMap(EventZone::getId, z -> z));

        if (dto.zones() != null) {
            for (ZoneDto z : dto.zones()) {
                if (z.id() != null && zoneById.containsKey(z.id())) {
                    // UPDATE in-place
                    var ez = zoneById.get(z.id());

                    // กันแก้ capacity ต่ำกว่ายอดที่ถูกจอง/ขายไปแล้ว
                    int reserved = registrationRepository.sumActiveQuantityByZone(z.id());
                    if (z.capacity() < reserved) {
                        throw new IllegalStateException(
                                "Capacity cannot be less than reserved (" + reserved + ") in zone " + z.id()
                        );
                    }

                    ez.setName(z.name());
                    ez.setCapacity(z.capacity());
                    ez.setPrice(z.price());
                    zoneRepo.save(ez);
                } else {
                    // CREATE new
                    var ez = new EventZone();
                    ez.setEvent(e);
                    ez.setName(z.name());
                    ez.setCapacity(z.capacity());
                    ez.setPrice(z.price());
                    zoneRepo.save(ez);
                }
            }
        }
    }




    /* ========== DELETE ========== */
    @Transactional
    public void delete(Integer id) {
        Event e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        // 1) ลบไฟล์ประกอบ (เช็ค null/ว่างกันพลาดได้)
        fileStorageService.deleteFile(e.getPosterImageUrl());
        fileStorageService.deleteFile(e.getDetailImageUrl());
        fileStorageService.deleteFile(e.getSeatmapImageUrl());

        // 2) ลบ register ที่ผูกกับอีเวนต์นี้ก่อน
        registrationRepository.deleteAllByEventCascade(id);

        // 3) ลบลูกตัวอื่น ๆ ของอีเวนต์ (ถ้ามี FK ไปหา session/zone ให้ลบ register ก่อนเสมอ)
        sessionRepo.deleteByEventId(id);
        zoneRepo.deleteByEventId(id);

        // 4) สุดท้ายลบ event
        eventRepo.delete(e);
    }


    /* ========== Helpers ========== */

    private static void applyCoreFields(Event e, EventUpsertRequest dto) {
        e.setTitle(dto.title());
        e.setCategory(dto.category());
        e.setLocation(dto.location());
        e.setStartDate(dto.startDate());
        e.setEndDate(dto.endDate());
        e.setStatus(dto.status());
        e.setSaleStartAt(dto.saleStartAt());
        e.setSaleEndAt(dto.saleEndAt());
        e.setSaleUntilSoldout(Boolean.TRUE.equals(dto.saleUntilSoldout()));
        e.setDoorOpenTime(dto.doorOpenTime());
        e.setDescription(dto.description());
    }

    private void setImagesFromUploadsOrDto(
            Event e, EventUpsertRequest dto,
            MultipartFile poster, MultipartFile detail, MultipartFile seatmap) {

        // --- poster ---
        if (poster != null && !poster.isEmpty()) {
            String newUrl = fileStorageService.replaceFile(e.getPosterImageUrl(), poster);
            e.setPosterImageUrl(newUrl);
        } else if (dto.posterImageUrl() == null) {
            // ผู้ใช้ตั้งใจลบ
            fileStorageService.deleteFile(e.getPosterImageUrl());
            e.setPosterImageUrl(null);
        } else {
            // ผู้ใช้ส่ง URL เดิม/ใหม่มา (จาก client)
            e.setPosterImageUrl(dto.posterImageUrl());
        }

        // --- detail ---
        if (detail != null && !detail.isEmpty()) {
            String newUrl = fileStorageService.replaceFile(e.getDetailImageUrl(), detail);
            e.setDetailImageUrl(newUrl);
        } else if (dto.detailImageUrl() == null) {
            fileStorageService.deleteFile(e.getDetailImageUrl());
            e.setDetailImageUrl(null);
        } else {
            e.setDetailImageUrl(dto.detailImageUrl());
        }

        // --- seatmap ---
        if (seatmap != null && !seatmap.isEmpty()) {
            String newUrl = fileStorageService.replaceFile(e.getSeatmapImageUrl(), seatmap);
            e.setSeatmapImageUrl(newUrl);
        } else if (dto.seatmapImageUrl() == null) {
            fileStorageService.deleteFile(e.getSeatmapImageUrl());
            e.setSeatmapImageUrl(null);
        } else {
            e.setSeatmapImageUrl(dto.seatmapImageUrl());
        }
    }
    /* ========== Read 2 (event+salestatus+pricezone)========== */
    @Transactional(readOnly = true)
    public EventDetailViewDto getView(Integer id) {
        Event e = eventRepo.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        // ===== 1. map sessions =====
        var sessions = e.getSessions().stream()
                .map(s -> new SessionDto(
                        s.getId(), s.getName(), s.getStartTime(), s.getStatus()
                )).toList();

        // ===== 2. ราคาทุกzone =====
        var prices = zoneRepo.findPricesByEventId(id);

        // ===== 3. คำนวณสถานะการขาย =====
        EventSaleStatus saleStatus = computeSaleStatus(e);

        // ===== 4. return DTO ใหม่ =====
        return new EventDetailViewDto(
                e.getId(), e.getTitle(), e.getCategory(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.getStatus(),
                e.getSaleStartAt(), e.getSaleEndAt(), e.isSaleUntilSoldout(),
                e.getDoorOpenTime(), e.getDescription(),
                e.getPosterImageUrl(), e.getDetailImageUrl(), e.getSeatmapImageUrl(),
                sessions,
                saleStatus,
                prices
        );
    }
    private EventSaleStatus computeSaleStatus(Event e) {
        LocalDateTime now = LocalDateTime.now();

        // 0) ปิดทั้งอีเวนต์
        if (e.getStatus() == Status.CLOSED) return EventSaleStatus.CLOSED;

        // 1) ยังไม่ถึงเวลาเริ่มขาย
        if (e.getSaleStartAt() != null && now.isBefore(e.getSaleStartAt())) {
            return EventSaleStatus.UPCOMING;
        }

        // 2) ถ้าไม่ได้ขายจนหมด และถึง/เลยเวลา saleEndAt แล้ว → ปิด (inclusive)
        if (!Boolean.TRUE.equals(e.isSaleUntilSoldout())
                && e.getSaleEndAt() != null
                && (now.isAfter(e.getSaleEndAt()) || now.isEqual(e.getSaleEndAt()))) {
            return EventSaleStatus.CLOSED;
        }

        // 3) (ออปชัน) ถ้าอยากปิดหลังงานจบจริง ๆ
        if (e.getEndDate() != null && LocalDate.now().isAfter(e.getEndDate())) {
            return EventSaleStatus.CLOSED;
        }

        // 4) อื่น ๆ = เปิดขาย
        return EventSaleStatus.OPEN;
    }




}
