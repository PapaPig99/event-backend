package com.example.eventproject.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EventPublicDto {
    private Integer id;
    private String title;
    private String subtitle;
    private String venue;
    private String bannerUrl;
    private String seatmapUrl;
    private List<String> quickNotes;
    private String descriptionHtml;
    private List<ShowtimeDto> showtimes;
    private Map<String, List<ZoneDto>> zonesByShow;

    @Data
    public static class ShowtimeDto {
        private String id;
        private String label;
        private String date;
        private String time;
    }

    @Data
    public static class ZoneDto {
        private String id;
        private String name;
        private Integer price;
        private String status;
    }
}
