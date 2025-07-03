package ru.otus.auth.service.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_role", schema = "auth")
public class UserRole {
    @EmbeddedId
    private UserRoleId id = new UserRoleId();

    @MapsId("roleId")
    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
