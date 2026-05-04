package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    Set<Long> likes = new HashSet<>();
    Set<Genre> genres = new HashSet<>();
    Rating mpa;
}
