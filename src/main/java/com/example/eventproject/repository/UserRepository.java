package com.example.eventproject.repository;

import com.example.eventproject.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.roles r WHERE u.email = :email")
    Optional<User> findWithRolesByEmail(@Param("email") String email);
}
