package com.example.eventproject.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
@Getter @Setter
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // code เช่น "USER", "ADMIN"
    @Column(nullable=false, unique=true)
    private String code;
}
