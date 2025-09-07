@Entity @Table(name="events") @Data
public class Event {
    @Id @GeneratedValue private Integer id;
    private String title;  private String description;  private String category;  private String location;
    private LocalDate startDate;  private LocalDate endDate;
    @Enumerated(EnumType.STRING) private Status status; // OPEN/CLOSED
    private LocalDateTime saleStartAt;  private LocalDateTime saleEndAt;
    private Boolean saleUntilSoldout;  private LocalTime doorOpenTime;
    private String posterImageUrl; private String detailImageUrl; private String seatmapImageUrl;
    // relations (optional to load lazily)
    @OneToMany(mappedBy="event") private List<EventSession> sessions = List.of();
    @OneToMany(mappedBy="event") private List<EventZone> zones = List.of();
    public enum Status { OPEN, CLOSED }
}
