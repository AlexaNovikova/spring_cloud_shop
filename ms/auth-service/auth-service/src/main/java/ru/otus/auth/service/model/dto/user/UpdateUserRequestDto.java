package ru.otus.auth.service.model.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]*$")
    private String lastName;
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]*$")
    private String firstName;
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]*$")
    private String secondName;
    @Pattern(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\\\.[A-Za-z0-9_-]+)*@\" \n" +
            "        + \"[^-][A-Za-z0-9-]+(\\\\.[A-Za-z0-9-]+)*(\\\\.[A-Za-z]{2,})$")
    private String email;
}
