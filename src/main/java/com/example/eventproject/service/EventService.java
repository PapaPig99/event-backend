package com.example.eventproject.service;

import com.example.eventproject.dto.*;
import com.example.eventproject.model.*;
import com.example.eventproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service หลักสำหรับจัดการ Event (สร้าง / อ่าน / อัปเดต / ลบ)
 * --------------------------------------------------------------
 * รองรับการสร้าง event พร้อม session และ zones (ทั้งจาก template และ custom)
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepo;
    private final EventSessionRepository sessionRepo;
    private final EventZoneRepository zoneRepo;
    private final FileStorageService fileStorageService;
    private final RegistrationRepository registrationRepository;
    private final ZoneTemplateService zoneTemplateService;

    /* ==========================================================
       READ : ดึงรายการ Event ทั้งหมด (หน้า Overview / Admin)
       ========================================================== */
    @Transactional(readOnly = true)
    public List<EventSummaryView> list() {
        return eventRepo.findAllByOrderByStartDateAsc();
    }

    /* ==========================================================
       READ : ดึงรายละเอียด Event พร้อม Sessions + Zones
       ========================================================== */
    @Transactional(readOnly = true)
    public EventDetailDto get(Integer id) {
        Event e = eventRepo.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        var allTemplates = zoneTemplateService.getAllTemplates();  // โหลดทั้งหมดก่อน
        var sessions = e.getSessions().stream()
                .map(s -> {
                    boolean isTemplate = isFromTemplate(s.getZones());

                    // หา templateIds → จากชื่อโซนที่ตรงกับ template
                    List<Integer> templateIds = isTemplate
                            ? s.getZones().stream()
                            .map(z -> allTemplates.stream()
                                    .filter(t -> t.name().equals(z.getName()))
                                    .map(ZoneTemplateDto::id)
                                    .findFirst()
                                    .orElse(null)
                            )
                            .filter(templateId -> templateId != null)
                            .distinct()
                            .toList()
                            : List.of();

                    return new SessionDto(
                            s.getId(),
                            s.getName(),
                            s.getStartTime(),
                            isTemplate,
                            templateIds,
                            s.getZones().stream()
                                    .map(z -> new ZoneDto(
                                            z.getId(),
                                            z.getName(),
                                            z.getGroupName(),
                                            z.getCapacity(),
                                            z.getPrice()
                                    ))
                                    .toList()
                    );
                })
                .toList();


        return new EventDetailDto(
                e.getId(), e.getTitle(), e.getCategory(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.getStatus(),
                e.getSaleStartAt(), e.getSaleEndAt(), e.isSaleUntilSoldout(),
                e.getDoorOpenTime(), e.getDescription(),
                e.getPosterImageUrl(), e.getSeatmapImageUrl(),
                e.getCreatedAt(), sessions
        );
    }

    /* ==========================================================
       CREATE : สร้าง Event ใหม่ + Sessions + Zones
       ========================================================== */
    @Transactional
    public Integer create(EventUpsertRequest dto,
                          MultipartFile poster, MultipartFile seatmap, String email) {

        Event e = new Event();
        applyCoreFields(e, dto);
        setImagesFromUploadsOrDto(e, dto, poster, seatmap);
        Event savedEvent = eventRepo.save(e);

        if (dto.sessions() != null) {
            for (SessionUpsertDto s : dto.sessions()) {

                EventSession session = new EventSession();
                session.setEvent(savedEvent);
                session.setName(s.name());
                session.setStartTime(s.startTime());

                EventSession savedSession = sessionRepo.save(session);

                // ถ้าเลือก template
                if (Boolean.TRUE.equals(s.useZoneTemplate())) {
                    zoneTemplateService.cloneSpecificTemplatesToSession(
                            savedSession.getId(),
                            s.templateIds()
                    );
                }

                // ถ้า custom
                else if (s.zones() != null && !s.zones().isEmpty()) {
                    for (ZoneDto z : s.zones()) {
                        EventZone zone = new EventZone();
                        zone.setSession(savedSession);
                        zone.setName(z.name());
                        zone.setGroupName(z.groupName());
                        zone.setCapacity(z.capacity());
                        zone.setPrice(z.price());
                        zoneRepo.save(zone);
                    }
                }
            }
        }

        return savedEvent.getId();
    }


    /* ==========================================================
       UPDATE : แก้ไข Event + Sessions + Zones
       ========================================================== */
    @Transactional
    public void update(Integer id, EventUpsertRequest dto,
                       MultipartFile poster, MultipartFile seatmap) {

        Event e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        applyCoreFields(e, dto);
        setImagesFromUploadsOrDto(e, dto, poster, seatmap);
        eventRepo.save(e);

        var existingSessions = sessionRepo.findByEventId(id)
                .stream().collect(Collectors.toMap(EventSession::getId, s -> s));

        if (dto.sessions() != null) {
            for (SessionUpsertDto s : dto.sessions()) {

                EventSession session = (s.id() != null && existingSessions.containsKey(s.id()))
                        ? existingSessions.get(s.id())
                        : new EventSession();

                session.setEvent(e);
                session.setName(s.name());
                session.setStartTime(s.startTime());
                EventSession savedSession = sessionRepo.save(session);

                // ลบ zone เดิม
                var oldZones = zoneRepo.findBySession_Id(savedSession.getId());
                zoneRepo.deleteAll(oldZones);

                // ใช้ template
                if (Boolean.TRUE.equals(s.useZoneTemplate())) {
                    zoneTemplateService.cloneSpecificTemplatesToSession(
                            savedSession.getId(),
                            s.templateIds()
                    );
                }

                // custom
                else if (s.zones() != null && !s.zones().isEmpty()) {
                    for (ZoneDto z : s.zones()) {
                        EventZone zone = new EventZone();
                        zone.setSession(savedSession);
                        zone.setName(z.name());
                        zone.setGroupName(z.groupName());
                        zone.setCapacity(z.capacity());
                        zone.setPrice(z.price());
                        zoneRepo.save(zone);
                    }
                }
            }
        }
    }


    /* ==========================================================
       DELETE : ลบ Event ทั้งหมด (รวม sessions / zones / regis)
       ========================================================== */
    @Transactional
    public void delete(Integer id) {
        Event e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        fileStorageService.deleteFile(e.getPosterImageUrl());
        fileStorageService.deleteFile(e.getSeatmapImageUrl());
        registrationRepository.deleteAllByEventCascade(id);
        sessionRepo.deleteByEvent_Id(id);
        eventRepo.delete(e);
    }

    /* ==========================================================
       UTILITIES : ฟังก์ชันช่วยภายใน
       ========================================================== */
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
            MultipartFile poster, MultipartFile seatmap) {

        if (poster != null && !poster.isEmpty()) {
            String newUrl = fileStorageService.replaceFile(e.getPosterImageUrl(), poster);
            e.setPosterImageUrl(newUrl);
        } else if (dto.posterImageUrl() == null) {
            fileStorageService.deleteFile(e.getPosterImageUrl());
            e.setPosterImageUrl(null);
        } else {
            e.setPosterImageUrl(dto.posterImageUrl());
        }

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

    /* ==========================================================
       READ (ฝั่ง Public View)
       ========================================================== */
    @Transactional(readOnly = true)
    public EventDetailViewDto getView(Integer id) {
        Event e = eventRepo.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        var allTemplates = zoneTemplateService.getAllTemplates();  // โหลดทั้งหมดก่อน
        var sessions = e.getSessions().stream()
                .map(s -> {
                    boolean isTemplate = isFromTemplate(s.getZones());

                    // หา templateIds → จากชื่อโซนที่ตรงกับ template
                    List<Integer> templateIds = isTemplate
                            ? s.getZones().stream()
                            .map(z -> allTemplates.stream()
                                    .filter(t -> t.name().equals(z.getName()))
                                    .map(ZoneTemplateDto::id)
                                    .findFirst()
                                    .orElse(null)
                            )
                            .filter(templateId -> templateId != null)
                            .distinct()
                            .toList()
                            : List.of();

                    return new SessionDto(
                            s.getId(),
                            s.getName(),
                            s.getStartTime(),
                            isTemplate,
                            templateIds,
                            s.getZones().stream()
                                    .map(z -> new ZoneDto(
                                            z.getId(),
                                            z.getName(),
                                            z.getGroupName(),
                                            z.getCapacity(),
                                            z.getPrice()
                                    ))
                                    .toList()
                    );
                })
                .toList();


        var prices = zoneRepo.findAll().stream()
                .map(z -> new PriceDto(z.getPrice()))
                .distinct()
                .toList();

        EventSaleStatus saleStatus = computeSaleStatus(e);

        return new EventDetailViewDto(
                e.getId(), e.getTitle(), e.getCategory(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.getStatus(),
                e.getSaleStartAt(), e.getSaleEndAt(), e.isSaleUntilSoldout(),
                e.getDoorOpenTime(), e.getDescription(),
                e.getPosterImageUrl(), e.getSeatmapImageUrl(),
                e.getCreatedAt(), sessions, saleStatus, prices
        );
    }

    /* ==========================================================
       HELPER FUNCTIONS
       ========================================================== */
    private EventSaleStatus computeSaleStatus(Event e) {
        LocalDateTime now = LocalDateTime.now();

        if (e.getStatus() == Status.CLOSED) return EventSaleStatus.CLOSED;
        if (e.getSaleStartAt() != null && now.isBefore(e.getSaleStartAt()))
            return EventSaleStatus.UPCOMING;
        if (!e.isSaleUntilSoldout() && e.getSaleEndAt() != null &&
                (now.isAfter(e.getSaleEndAt()) || now.isEqual(e.getSaleEndAt())))
            return EventSaleStatus.CLOSED;
        if (e.getEndDate() != null && LocalDate.now().isAfter(e.getEndDate()))
            return EventSaleStatus.CLOSED;
        return EventSaleStatus.OPEN;
    }

    private boolean isFromTemplate(List<EventZone> zones) {
        if (zones == null || zones.isEmpty()) return false;

        List<String> templateNames = zoneTemplateService.getAllTemplates()
                .stream()
                .map(ZoneTemplateDto::name)
                .toList();

        long matchCount = zones.stream()
                .filter(z -> templateNames.contains(z.getName()))
                .count();

        return (double) matchCount / zones.size() >= 0.8;
    }

}
