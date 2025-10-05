package com.example.eventproject.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EventDetailDto {
    private Integer id;
    private String title;
    private String subtitle;
    private String venue;
    private String bannerUrl;
    private String seatmapUrl;
    private List<String> quickNotes;
    private String descriptionHtml;

    private List<ShowtimeDto> showtimes;           // รอบการแสดง
    private Map<String, List<ZoneDto>> zonesByShow; // โซนแยกตามรอบ (key = showId)

    @Data
    public static class ShowtimeDto {
        private String id;     // เช่น "st1"
        private String label;  // "ศุกร์ 11 ต.ค. 2568 • 19:00 น."
        private String date;   // "2025-10-11"
        private String time;   // "19:00"
    }

    @Data
    public static class ZoneDto {
        private String id;
        private String name;
        private Integer price;
        private String status; // available | few | soldout
    }
}
