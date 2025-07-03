package ru.otus.auth.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.auth.service.model.entity.User;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByLastNameAndFirstNameAndSecondNameAndEmail(String lastName, String firstName, String secondName, String email);
}