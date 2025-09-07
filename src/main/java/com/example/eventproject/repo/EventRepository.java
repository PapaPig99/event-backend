package com.example.eventproject.repo;

import com.example.eventproject.model.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findAllByOrderByStartDateAsc();
    
    @EntityGraph(attributePaths = {"sessions", "zones"})
    Optional<Event> findWithSessionsAndZonesById(Integer id);
}
