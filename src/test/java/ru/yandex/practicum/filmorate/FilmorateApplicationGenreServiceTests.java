package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(GenreDbStorage.class)
class FilmorateApplicationGenreServiceTests {

    private final GenreDbStorage genreDbStorage;

    @Test
    public void testFindGenreById() {
        Optional<Genre> genreOptional = Optional.ofNullable(genreDbStorage.getGenreById(1L));

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testCreateGenre() {
        Genre genreToCreate = new Genre(10L, "Test Genre");

        Genre createdGenre = genreDbStorage.createGenre(genreToCreate);

        assertThat(createdGenre)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Test Genre")
                .extracting(Genre::getId)
                .isNotNull();

        Optional<Genre> retrievedGenre = Optional.ofNullable(genreDbStorage.getGenreById(createdGenre.getId()));
        assertThat(retrievedGenre)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("name", "Test Genre")
                );
    }

    @Test
    public void testGetAllGenres() {
        Collection<Genre> allGenres = genreDbStorage.getAllGenres();

        assertThat(allGenres)
                .isNotEmpty()
                .hasSize(6)
                .extracting("name")
                .containsExactlyInAnyOrder("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }
}
