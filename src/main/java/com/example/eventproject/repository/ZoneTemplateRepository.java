package com.example.eventproject.repository;

import com.example.eventproject.model.ZoneTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneTemplateRepository extends JpaRepository<ZoneTemplate, Integer> {
}
