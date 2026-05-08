package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    @Email(message = "Введенный email не соответствует формату email-адресов!")
    private String email;
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы!")
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем!")
    private LocalDate birthday;
}
