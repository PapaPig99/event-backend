package com.example.eventproject.service;

import com.example.eventproject.model.User;
import com.example.eventproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /* ==========================================================
       GET ALL USERS
       ========================================================== */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /* ==========================================================
       GET USERS BY ROLE CODE
       ========================================================== */
    public List<User> getUsersByRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("Role code is required");
        }

        // Normalize input ให้เป็น uppercase เช่น "admin" → "ADMIN"
        String normalizedRole = roleCode.trim().toUpperCase();

        List<User> users = userRepository.findAllByRoleCode(normalizedRole);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users found for role: " + normalizedRole);
        }
        return users;
    }

    /* ==========================================================
       GET GUEST USERS
       ========================================================== */
    public List<User> getAllGuests() {
        return userRepository.findAllByRoleCode("GUEST");
    }
}
