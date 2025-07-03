package ru.otus.auth.service.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "user", schema = "auth")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String lastName;
    private String firstName;
    private String secondName;
    private String email;
    private OffsetDateTime createdAt;
    private OffsetDateTime blockedAt;
}