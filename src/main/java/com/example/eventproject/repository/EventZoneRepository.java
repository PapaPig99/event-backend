package com.example.eventproject.repository;

import com.example.eventproject.dto.ZoneDto;
import com.example.eventproject.model.EventZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventZoneRepository extends JpaRepository<EventZone, Integer> {

    @Query("""
        select new com.example.eventproject.dto.ZoneDto(
            z.id, z.name, z.capacity, z.price
        )
        from EventZone z
        where z.event.id = :eventId
        order by z.name asc
        """)
    List<ZoneDto> findAllDtosByEventId(Integer eventId);
}
