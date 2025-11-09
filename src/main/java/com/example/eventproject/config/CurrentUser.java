package com.example.eventproject.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * ตัวแทนผู้ใช้ปัจจุบันที่อยู่ใน SecurityContext
 */

public class CurrentUser implements UserDetails {

    private final String email;   // ใช้แทน id
    private final String name;
    private final String role;
    private final List<? extends GrantedAuthority> authorities;

    public CurrentUser(String email, String name, String role, List<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.authorities = authorities;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
