package com.example.eventproject.dto;

import com.example.eventproject.model.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private String email;
    private String name;
    private String roleCode;      //แสดง role "ADMIN" / "USER"

    public static UserDto from(User u) {
        if (u == null) return null;
        return UserDto.builder()
                .email(u.getEmail())
                .name(u.getName())
                .roleCode(u.getRole() != null ? u.getRole().getCode() : null)
                .build();
    }
}
