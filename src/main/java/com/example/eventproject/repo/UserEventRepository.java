package com.example.eventproject.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eventproject.model.UserEvent;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
}
