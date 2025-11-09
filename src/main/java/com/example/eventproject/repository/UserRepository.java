package com.example.eventproject.repository;

import com.example.eventproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /** ค้นหาผู้ใช้ด้วยอีเมล (primary key) */
    Optional<User> findByEmail(String email);

    /** ตรวจสอบว่ามีอีเมลนี้อยู่ในระบบแล้วหรือไม่ */
    boolean existsByEmail(String email);
}
