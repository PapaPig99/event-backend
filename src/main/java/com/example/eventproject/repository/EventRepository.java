package com.example.eventproject.repository;

import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<EventSummaryView> findAllByOrderByStartDateAsc();

    @Query("""
    SELECT e FROM Event e
    LEFT JOIN FETCH e.sessions s
    LEFT JOIN FETCH s.zones
    WHERE e.id = :id
""")
    Optional<Event> findDetailById(Integer id);

}
