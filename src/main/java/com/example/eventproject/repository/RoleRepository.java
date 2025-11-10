package com.example.eventproject.repository;

import com.example.eventproject.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /** ค้นหา role ด้วยรหัส code "ADMIN", "USER" , "GUEST" */
    Optional<Role> findByCode(String code);
}
