package ru.otus.auth.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.otus.auth.service.model.dto.reg.RegRequestDto;
import ru.otus.auth.service.model.entity.Identifier;
import ru.otus.auth.service.model.entity.User;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RegisterMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    User toUser(RegRequestDto dto);

    Identifier toIdentifier(UUID userId, String login, String secret);
}
