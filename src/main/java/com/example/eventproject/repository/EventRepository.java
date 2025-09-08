package com.example.eventproject.repository;

import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.model.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    @EntityGraph(attributePaths = {"sessions", "zones"})
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findDetailById(Integer id);

    List<EventSummaryView> findAllByOrderByStartDateAsc();
}

