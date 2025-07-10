package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FilmDbStorage.class, FilmRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStoragePopularTest {

    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;

    @BeforeEach
    void setUp() {

        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM ratings");


        initTestData();
    }

    private void initTestData() {

        jdbcTemplate.update("INSERT INTO ratings (id, name) VALUES (1, 'G'), (2, 'PG')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Комедия'), (2, 'Драма')");


        jdbcTemplate.update("""
            INSERT INTO films (id, name, description, releaseDate, duration, id_rating) VALUES
                (1, 'Film 1', 'Desc 1', ?, 120, 1),
                (2, 'Film 2', 'Desc 2', ?, 130, 2),
                (3, 'Film 3', 'Desc 3', ?, 140, 1),
                (4, 'Film 4', 'Desc 4', ?, 150, 2)
            """,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2021, 1, 1));


        jdbcTemplate.update("INSERT INTO films_genres (id_film, id_genre) VALUES (1, 1), (2, 2), (3, 1), (4, 2)");


        jdbcTemplate.update("""
            INSERT INTO likes (id_film, id_user) VALUES
                (1, 1), (1, 2), (1, 3),  -- Film 1: 3 лайка
                (2, 1), (2, 2),           -- Film 2: 2 лайка
                (3, 1),                    -- Film 3: 1 лайк
                (4, 1), (4, 2), (4, 3), (4, 4) -- Film 4: 4 лайка
            """);
    }

    @Test
    void mostPopular_shouldReturnFilmsOrderedByLikes() {
        Collection<Film> films = filmDbStorage.mostPopular(3, null, null);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(4, 1, 2);
    }

    @Test
    void mostPopular_shouldFilterByGenre() {
        Collection<Film> films = filmDbStorage.mostPopular(2, 1, null);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(1, 3);
    }

    @Test
    void mostPopular_shouldFilterByYear() {
        Collection<Film> films = filmDbStorage.mostPopular(10, null, 2020);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(1, 3);
    }

    @Test
    void mostPopular_shouldFilterByGenreAndYear() {
        Collection<Film> films = filmDbStorage.mostPopular(10, 2, 2021);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(4, 2);
    }

    @Test
    void mostPopular_shouldReturnLimitedCount() {
        Collection<Film> films = filmDbStorage.mostPopular(2, null, null);

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getId)
                .containsExactly(4, 1);
    }
}