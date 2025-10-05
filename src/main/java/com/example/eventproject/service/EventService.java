package com.example.eventproject.service;

import com.example.eventproject.dto.*;
import com.example.eventproject.model.Event;
import com.example.eventproject.model.EventSession;
import com.example.eventproject.model.EventZone;
import com.example.eventproject.repository.EventRepository;
import com.example.eventproject.repository.EventSessionRepository;
import com.example.eventproject.repository.EventZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepo;
    private final EventSessionRepository sessionRepo;
    private final EventZoneRepository zoneRepo;
    private final FileStorageService fileStorageService;

    /* ========== READ (Admin) ========== */

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
                        s.getId(), s.getName(), s.getStartTime(), s.getStatus()
                )).toList();

        var zones = e.getZones().stream()
                .map(z -> new ZoneDto(z.getId(), z.getName(), z.getCapacity(), z.getPrice()))
                .toList();

        return new EventDetailDto(
                e.getId(), e.getTitle(), e.getCategory(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.getStatus(),
                e.getSaleStartAt(), e.getSaleEndAt(), e.isSaleUntilSoldout(),
                e.getDoorOpenTime(), e.getDescription(),
                e.getPosterImageUrl(), e.getDetailImageUrl(),
                e.getSeatmapImageUrl(), sessions, zones
        );
    }

    /* ========== READ (User / Public) ========== */
    @Transactional(readOnly = true)
    public EventPublicDto getPublic(Integer id) {
        Event e = eventRepo.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        EventPublicDto dto = new EventPublicDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setSubtitle(e.getCategory());
        dto.setVenue(e.getLocation());
        dto.setBannerUrl(e.getPosterImageUrl());
        dto.setSeatmapUrl(e.getSeatmapImageUrl());
        dto.setQuickNotes(List.of(
                "ประตูเปิดก่อนการแสดง ~ 60 นาที",
                "บัตร 1 ใบต่อ 1 ที่นั่ง"
        ));
        dto.setDescriptionHtml(e.getDescription() != null ? "<p>" + e.getDescription() + "</p>" : "");

        // map sessions → showtimes
        List<EventPublicDto.ShowtimeDto> showtimes = e.getSessions().stream().map(s -> {
            EventPublicDto.ShowtimeDto st = new EventPublicDto.ShowtimeDto();
            st.setId("s" + s.getId());
            String date = s.getStartTime() != null ? s.getStartTime().toLocalDate().toString() : "";
            String time = s.getStartTime() != null ? s.getStartTime().toLocalTime().toString() : "";
            st.setDate(date);
            st.setTime(time);
            st.setLabel(s.getName() != null && !s.getName().isBlank()
                    ? s.getName()
                    : (date.isEmpty() && time.isEmpty() ? "-" : (date + " • " + time)));
            return st;
        }).toList();
        dto.setShowtimes(showtimes);

        // map zones (สมมติทุก session ใช้ zone เหมือนกัน)
        List<EventPublicDto.ZoneDto> zoneList = e.getZones().stream().map(z -> {
            EventPublicDto.ZoneDto zz = new EventPublicDto.ZoneDto();
            zz.setId("z" + z.getId());
            zz.setName(z.getName());
            zz.setPrice(z.getPrice());
            // เดาสถานะจาก capacity
            String status = (z.getCapacity() == null || z.getCapacity() > 50) ? "available"
                    : (z.getCapacity() > 0 ? "few" : "soldout");
            zz.setStatus(status);
            return zz;
        }).toList();

        Map<String, List<EventPublicDto.ZoneDto>> zonesByShow = new LinkedHashMap<>();
        for (EventPublicDto.ShowtimeDto st : showtimes) {
            zonesByShow.put(st.getId(), zoneList);
        }
        dto.setZonesByShow(zonesByShow);

        return dto;
    }

    /* ========== CREATE ========== */

    @Transactional
    public Integer create(EventUpsertRequest dto,
                          MultipartFile poster, MultipartFile detail, MultipartFile seatmap) {
        Event e = new Event();
        applyCoreFields(e, dto);
        setImagesFromUploadsOrDto(e, dto, poster, detail, seatmap);

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

        applyCoreFields(e, dto);
        setImagesFromUploadsOrDto(e, dto, poster, detail, seatmap);

        Event saved = eventRepo.save(e);

        sessionRepo.deleteByEventId(saved.getId());
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

        zoneRepo.deleteByEventId(saved.getId());
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
    }

    /* ========== DELETE ========== */

    @Transactional
    public void delete(Integer id) {
        Event e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        fileStorageService.deleteFile(e.getPosterImageUrl());
        fileStorageService.deleteFile(e.getDetailImageUrl());
        fileStorageService.deleteFile(e.getSeatmapImageUrl());

        sessionRepo.deleteByEventId(id);
        zoneRepo.deleteByEventId(id);
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
        e.setCreatedByUserId(dto.createdByUserId());
    }

    private void setImagesFromUploadsOrDto(
            Event e, EventUpsertRequest dto,
            MultipartFile poster, MultipartFile detail, MultipartFile seatmap) {

        // --- poster ---
        if (poster != null && !poster.isEmpty()) {
            String newUrl = fileStorageService.replaceFile(e.getPosterImageUrl(), poster);
            e.setPosterImageUrl(newUrl);
        } else if (dto.posterImageUrl() == null) {
            fileStorageService.deleteFile(e.getPosterImageUrl());
            e.setPosterImageUrl(null);
        } else {
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
}
