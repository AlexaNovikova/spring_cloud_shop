package ru.otus.auth.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.otus.auth.shared.model.AuthContext;
import ru.otus.auth.service.model.dto.auth.UserDto;
import ru.otus.auth.service.model.dto.user.CreateUserRequestDto;
import ru.otus.auth.service.model.dto.user.UserResponseDto;
import ru.otus.auth.service.model.entity.User;
import ru.otus.common.ShopUser;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    AuthContext toCtx(User user);

    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    User toEntity(CreateUserRequestDto dto);

    UserResponseDto toDto(User user);

    List<UserResponseDto> toDtos(List<User> users);

    UserDto toDto(ShopUser shopUser);

    ShopUser toUserCtx(AuthContext authContext);
}
