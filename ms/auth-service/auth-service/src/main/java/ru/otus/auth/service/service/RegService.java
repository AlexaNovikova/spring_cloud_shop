package ru.otus.auth.service.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.otus.auth.service.mapper.RegisterMapper;
import ru.otus.auth.service.model.dto.reg.RegRequestDto;
import ru.otus.auth.service.model.dto.reg.RegResponseDto;
import ru.otus.auth.service.model.entity.UserRole;
import ru.otus.auth.service.model.entity.UserRoleId;
import ru.otus.auth.service.repository.IdentifierRepository;
import ru.otus.auth.service.repository.RoleRepository;
import ru.otus.auth.service.repository.UserRepository;
import ru.otus.auth.service.repository.UserRoleRepository;
import ru.otus.common.Roles;
import ru.otus.lib.kafka.model.CreateAccountModel;
import ru.otus.lib.kafka.service.BusinessTopics;
import ru.otus.lib.kafka.service.KafkaProducerService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegService {

    private final UserRepository userRepository;
    private final IdentifierRepository identifierRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final KafkaProducerService kafkaProducerService;

    private final RegisterMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegResponseDto register(RegRequestDto dto) {
        var email = dto.getEmail();
        var existingIdentifier = identifierRepository.findByLogin(email);
        if (existingIdentifier != null) {
            log.error("User with email {} already exists", email);
            throw new EntityExistsException("Пользователь с таким email '" + email + "' уже существует");
        }

        var user = mapper.toUser(dto);
        var createdUser = userRepository.save(user);

        var secret = passwordEncoder.encode(dto.getPassword());

        var identifier = mapper.toIdentifier(createdUser.getId(), email, secret);
        identifierRepository.save(identifier);

        var clientRole = roleRepository.findByName(Roles.CLIENT.name());
        if (clientRole.isPresent()) {
            var userRole = new UserRole();
            userRole.setUser(createdUser);
            userRole.setRole(clientRole.get());
            userRoleRepository.save(userRole);
        }

        kafkaProducerService.send(BusinessTopics.PAYMENT_NEW_ACCOUNT, new CreateAccountModel(createdUser.getId()));

        return new RegResponseDto(createdUser.getId());
    }
}
