package com.example.eventproject.repository;

import com.example.eventproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /** ค้นหาผู้ใช้ด้วยอีเมล (primary key) */
    Optional<User> findByEmail(String email);

    /** ตรวจสอบว่ามีอีเมลนี้อยู่ในระบบแล้วหรือไม่ */
    boolean existsByEmail(String email);

    /** ดึงผู้ใช้ทั้งหมดที่มี role code ที่กำหนด (เช่น 'ADMIN', 'USER', 'GUEST') */
    @Query("SELECT u FROM User u WHERE u.role.code = :roleCode")
    List<User> findAllByRoleCode(String roleCode);
}
