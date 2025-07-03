package ru.otus.auth.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.auth.service.model.entity.Identifier;

import java.util.UUID;

@Repository
public interface IdentifierRepository extends JpaRepository<Identifier, UUID> {

    Identifier findByLogin(String login);
}
