package ru.otus.auth.service.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "identifier", schema = "auth")
public class Identifier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private String login;
    private Boolean blocked;
    private String secret;
}
