package ru.otus.auth.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.auth.service.model.entity.UserRole;
import ru.otus.auth.service.model.entity.UserRoleId;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    @Query("SELECT r.name FROM UserRole ur JOIN ur.role r WHERE ur.id.userId = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") UUID userId);
}
