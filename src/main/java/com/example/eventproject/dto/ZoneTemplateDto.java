package com.example.eventproject.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneTemplateDto {
    private Integer id;
    private String name;
    private String groupName;
    private Integer capacity;
    private BigDecimal price;
    private Boolean hasSeatNumbers;
}
