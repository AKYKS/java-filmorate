package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.dao.RatingDbStorage;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(RatingDbStorage.class)
class FilmorateApplicationRatingServiceTests {

    private final RatingDbStorage ratingDbStorage;

    @Test
    public void testFindRatingById() {
        Optional<Rating> ratingOptional = Optional.ofNullable(ratingDbStorage.getRatingById(1L));

        assertThat(ratingOptional)
                .isPresent()
                .hasValueSatisfying(rating ->
                        assertThat(rating).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testCreateRating() {
        Rating ratingToCreate = new Rating(10L, "Test Rating");

        Rating createdRating = ratingDbStorage.createRating(ratingToCreate);

        assertThat(createdRating)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Test Rating")
                .extracting(Rating::getId)
                .isNotNull();

        Optional<Rating> retrievedRating = Optional.ofNullable(ratingDbStorage.getRatingById(createdRating.getId()));
        assertThat(retrievedRating)
                .isPresent()
                .hasValueSatisfying(rating ->
                        assertThat(rating).hasFieldOrPropertyWithValue("name", "Test Rating")
                );
    }

    @Test
    public void testGetAllRating() {
        Collection<Rating> allGenres = ratingDbStorage.getAllRatings();

        assertThat(allGenres)
                .isNotEmpty()
                .hasSize(5)
                .extracting("name")
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }
}
