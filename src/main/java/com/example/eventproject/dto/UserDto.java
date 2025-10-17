package com.example.eventproject.dto;

import com.example.eventproject.model.User;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String organization;

    public static UserDto from(User u) {
        if (u == null) return null;
        return UserDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .organization(u.getOrganization())
                .build();
    }
}
