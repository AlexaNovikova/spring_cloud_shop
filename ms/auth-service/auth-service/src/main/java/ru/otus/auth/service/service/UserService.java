package ru.otus.auth.service.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.otus.auth.service.mapper.UserMapper;
import ru.otus.auth.service.model.dto.user.CreateUserRequestDto;
import ru.otus.auth.service.model.dto.user.UpdateUserRequestDto;
import ru.otus.auth.service.model.dto.user.UserListResponseDto;
import ru.otus.auth.service.model.dto.user.UserResponseDto;
import ru.otus.auth.service.model.entity.User;
import ru.otus.auth.service.repository.IdentifierRepository;
import ru.otus.auth.service.repository.UserRepository;
import ru.otus.auth.service.repository.UserRoleRepository;
import ru.otus.auth.shared.model.AuthContext;
import ru.otus.common.ShopUser;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final IdentifierRepository identifierRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public UserResponseDto create(CreateUserRequestDto dto) {
        var existingUser = repository.findByLastNameAndFirstNameAndSecondNameAndEmail(
                dto.getLastName(), dto.getFirstName(), dto.getSecondName(),
                dto.getEmail());
        if (existingUser != null) {
            log.error("User with the same info already exists: {}", dto);
            throw new EntityExistsException("User already exists in the system"); //todo process exception
        }

        var user = mapper.toEntity(dto);
        var createdUser = repository.save(user);
        return mapper.toDto(createdUser);
    }

    @Transactional
    public UserResponseDto getById(UUID id, ShopUser shopUser) {
        if (!Objects.equals(shopUser.getId(), id)) {
            log.error("Profile viewing is denied for user with id: {}", id);
            throw new AccessDeniedException("Profile viewing is forbidden");
        }
        var user = getUser(id);
        return mapper.toDto(user);
    }

    @Transactional
    public UserResponseDto update(UUID id, UpdateUserRequestDto dto, ShopUser shopUser) {
        if (!Objects.equals(shopUser.getId(), id)) {
            log.error("Profile update is denied for user with id: {}", id);
            throw new AccessDeniedException("Profile viewing is forbidden");
        }

        var user = getUser(id);

        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getSecondName() != null) {
            user.setSecondName(dto.getSecondName());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        repository.save(user);
        return mapper.toDto(user);
    }

    @Transactional
    public void delete(UUID id) {
        checkUserExists(id);
        repository.deleteById(id);
    }

    public UserListResponseDto getAll() {
        var users = repository.findAll(Sort.by("lastName", "firstName", "secondName"));
        var usersDto = mapper.toDtos(users);
        return new UserListResponseDto(usersDto);
    }

    private User getUser(UUID id) {
        var user = repository.findById(id);
        if (user.isEmpty()) {
            log.error("User with id {} not found", id);
            throw new NoSuchElementException("User with id " + id + " not found"); //todo process exception
        }

        return user.get();
    }

    private void checkUserExists(UUID id) {
        var userExists = repository.existsById(id);
        if (!userExists) {
            log.error("User with id {} not found", id);
            throw new NoSuchElementException("User with id " + id + " not found"); //todo process exception
        }
    }

    public UserDetailsService userDetailsService() {
        return this::getByLogin;
    }

    public AuthContext getByLogin(String login) {
        var identifier = identifierRepository.findByLogin(login);
        if (identifier == null) {
            log.error("Identifier with login {} not found", login);
            throw new UsernameNotFoundException("User not found");
        }

        var user = repository.findById(identifier.getUserId());
        if (user == null || user.isEmpty()) {
            log.error("User with login {} not found", login);
            throw new UsernameNotFoundException("User not found");
        }

        var roles = userRoleRepository.findRoleNamesByUserId(identifier.getUserId());

        var authContext = mapper.toCtx(user.get());
        authContext.setLogin(identifier.getLogin());
        authContext.setFirstName(user.get().getFirstName());
        authContext.setPassword(identifier.getSecret());
        authContext.setRoles(roles);
        return authContext;
    }

}
